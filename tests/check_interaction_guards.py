"""Exercise the compiled interaction guards without launching Minecraft.

Only guard instructions are interpreted. Unexpected calls/opcodes fail closed;
reaching getItemInHand marks entry to the existing server implementation.
This checks client prediction and server reachability, not live teleportation.
"""

import re
import subprocess
import sys


def methods(jar):
    output = subprocess.check_output(
        ["javap", "-c", "-p", "-classpath", jar,
         "com.personalwaystone.PersonalWaystoneItem"], text=True
    )
    result = {}
    current = None
    for line in output.splitlines():
        header = re.search(r"\b(useOn|use)\(", line)
        if header:
            current = header.group(1)
            result[current] = {}
        elif re.match(r"  (public|private|protected) ", line):
            current = None
        instruction = re.match(r"\s+(\d+):\s+(\w+)\s*(.*)", line)
        if current and instruction:
            offset, opcode, args = instruction.groups()
            result[current][int(offset)] = (opcode, args)
    assert set(result) == {"useOn", "use"}, "Missing interaction methods"
    return result


def evaluate(code, method, client, sneak, present=True):
    player = "player" if present else None
    locals_ = ({0: "item", 1: "context"} if method == "useOn"
               else {0: "item", 1: "world", 2: player, 3: "hand"})
    stack = []
    offsets = list(code)
    index = 0
    for _ in range(200):
        opcode, args = code[offsets[index]]
        jump = None
        if opcode.startswith("aload"):
            slot = int(opcode.split("_")[1] if "_" in opcode else args)
            stack.append(locals_[slot])
        elif opcode.startswith("astore"):
            slot = int(opcode.split("_")[1] if "_" in opcode else args)
            locals_[slot] = stack.pop()
        elif opcode == "invokevirtual":
            call = args.split("// Method ")[1].split(":")[0]
            name = call.rsplit(".", 1)[1]
            if name == "getItemInHand":
                return "SERVER_BODY"
            receiver = stack.pop()
            assert receiver is not None, "Null receiver"
            if name == "getPlayer":
                stack.append(player)
            elif name == "getLevel":
                stack.append("world")
            elif name == "isShiftKeyDown":
                stack.append(sneak)
            elif name == "isClientSide":
                stack.append(client)
            else:
                raise AssertionError(f"Unexpected call before guard: {call}")
        elif opcode in {"ifnull", "ifeq", "ifne"}:
            value = stack.pop()
            take = (value is None if opcode == "ifnull"
                    else not value if opcode == "ifeq" else bool(value))
            if take:
                jump = int(args)
        elif opcode == "getstatic":
            field = args.split("// Field ")[1].split(":")[0]
            assert "net/minecraft/world/InteractionResult." in field, field
            stack.append(field.rsplit(".", 1)[1])
        elif opcode == "areturn":
            return stack.pop()
        # The repaired reference JAR has diagnostic logging before its guards.
        elif opcode in {"ldc", "ldc_w"}:
            assert "// String " in args, args
            stack.append(args.split("// String ")[1])
        elif opcode.startswith("iconst_"):
            stack.append(int(opcode.split("_")[1]))
        elif opcode == "anewarray":
            assert "java/lang/Object" in args, args
            stack.append([None] * stack.pop())
        elif opcode == "dup":
            stack.append(stack[-1])
        elif opcode == "aastore":
            value, slot, array = stack.pop(), stack.pop(), stack.pop()
            array[slot] = value
        elif opcode == "goto":
            jump = int(args)
        elif opcode == "invokestatic":
            call = args.split("// Method ")[1].split(":")[0]
            if call == "java/lang/Boolean.valueOf":
                pass  # Boxing does not change the modeled boolean.
            elif call == "debug":
                stack.pop()
                stack.pop()
            else:
                raise AssertionError(f"Unexpected static call before guard: {call}")
        else:
            raise AssertionError(f"Unexpected guard opcode: {opcode}")
        index = offsets.index(jump) if jump is not None else index + 1
    raise AssertionError("Guard failed to terminate")


def main(jar):
    compiled = methods(jar)
    checked = 0
    for client in (False, True):
        for present, sneak in ((False, False), (True, False), (True, True)):
            expected = ("PASS" if not present or not sneak
                        else "SUCCESS" if client else "SERVER_BODY")
            actual = evaluate(compiled["useOn"], "useOn", client, sneak, present)
            assert actual == expected, ("useOn", client, present, sneak, actual, expected)
            checked += 1
        for sneak in (False, True):
            expected = "SUCCESS" if client else "SERVER_BODY"
            actual = evaluate(compiled["use"], "use", client, sneak)
            assert actual == expected, ("use", client, sneak, actual, expected)
            checked += 1
    print(f"PASS: {checked} compiled client/server interaction guard cases")


if __name__ == "__main__":
    main(sys.argv[1])
