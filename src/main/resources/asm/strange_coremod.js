function initializeCoreMod() {

    var ASM_HOOKS = "svenhjol/strange/base/StrangeAsmHooks";
    var ASM = Java.type('net.minecraftforge.coremod.api.ASMAPI');
    var Opcodes = Java.type('org.objectweb.asm.Opcodes');
    var InsnNode = Java.type('org.objectweb.asm.tree.InsnNode');
    var InsnList = Java.type('org.objectweb.asm.tree.InsnList');
    var VarInsnNode = Java.type('org.objectweb.asm.tree.VarInsnNode');
    var FieldInsnNode = Java.type('org.objectweb.asm.tree.FieldInsnNode');
    var MethodInsnNode = Java.type('org.objectweb.asm.tree.MethodInsnNode');
    var JumpInsnNode = Java.type('org.objectweb.asm.tree.JumpInsnNode');
    var LabelNode = Java.type('org.objectweb.asm.tree.LabelNode');

    return {

        /*
         * EnchantmentHelper: prevent custom treasure enchantments added by the helper
         */
        'EnchantmentHelper': {
            target: {
                'type': 'METHOD',
                'class': 'net.minecraft.enchantment.EnchantmentHelper',
                'methodName': 'func_77504_a', // addRandomEnchantment
                'methodDesc': '(Ljava/util/Random;Lnet/minecraft/item/ItemStack;IZ)Lnet/minecraft/item/ItemStack;'
            },
            transformer: function(method) {
                var didThing = false;
                var arrayLength = method.instructions.size();
                var newInstructions = new InsnList();
                for (var i = 0; i < arrayLength; ++i) {
                    var instruction = method.instructions.get(i)

                    if (instruction.getOpcode() == Opcodes.ASTORE) {
                        var label = new LabelNode();
                        newInstructions.add(new VarInsnNode(Opcodes.ALOAD, 4));
                        newInstructions.add(new VarInsnNode(Opcodes.ALOAD, 1));
                        newInstructions.add(new MethodInsnNode(Opcodes.INVOKESTATIC, ASM_HOOKS, "canApplyEnchantments", "(Ljava/util/List;Lnet/minecraft/item/ItemStack;)Z", false));
                        newInstructions.add(new JumpInsnNode(Opcodes.IFNE, label));
                        newInstructions.add(new VarInsnNode(Opcodes.ALOAD, 1));
                        newInstructions.add(new InsnNode(Opcodes.ARETURN));
                        newInstructions.add(label);

                        method.instructions.insert(instruction, newInstructions);
                        didThing = true;
                        break;
                    }
                }

                if (didThing) {
                    print("[Strange ASM] Transformed EnchantmentHelper");
                } else {
                    print("[Strange ASM] Failed to transform EnchantmentHelper")
                }

                return method;
            }
        },

        /*
         * EnchantRandomly: prevent custom treasure enchantments being added by loot function
         */
        'EnchantRandomly': {
            target: {
                'type': 'METHOD',
                'class': 'net.minecraft.world.storage.loot.functions.EnchantRandomly',
                'methodName': 'func_215859_a', // doApply
                'methodDesc': '(Lnet/minecraft/item/ItemStack;Lnet/minecraft/world/storage/loot/LootContext;)Lnet/minecraft/item/ItemStack;'
            },
            transformer: function(method) {
                var didThing = false;
                var arrayLength = method.instructions.size();
                var newInstructions = new InsnList();
                for (var i = 0; i < arrayLength; ++i) {
                    var instruction = method.instructions.get(i)

                    if (instruction.getOpcode() == Opcodes.ISTORE
                        && instruction.var == 5) {
                        var label = new LabelNode();
                        newInstructions.add(new VarInsnNode(Opcodes.ALOAD, 4));
                        newInstructions.add(new VarInsnNode(Opcodes.ALOAD, 1));
                        newInstructions.add(new MethodInsnNode(Opcodes.INVOKESTATIC, ASM_HOOKS, "canApplyEnchantment", "(Lnet/minecraft/enchantment/Enchantment;Lnet/minecraft/item/ItemStack;)Z", false));
                        newInstructions.add(new JumpInsnNode(Opcodes.IFNE, label));
                        newInstructions.add(new VarInsnNode(Opcodes.ALOAD, 1));
                        newInstructions.add(new InsnNode(Opcodes.ARETURN));
                        newInstructions.add(label);

                        method.instructions.insert(instruction, newInstructions);
                        didThing = true;
                        break;
                    }
                }

                if (didThing) {
                    print("[Strange ASM] Transformed EnchantRandomly");
                } else {
                    print("[Strange ASM] Failed to transform EnchantRandomly")
                }

                return method;
            }
        }
    }
}