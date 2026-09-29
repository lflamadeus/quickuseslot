package com.lai.quickuseslot;

import net.minecraftforge.fml.InterModComms;
import top.theillusivec4.curios.api.SlotTypeMessage;

/**
 * Declares the quick use slots to Curios.
 *
 * <p>Curios learns about player slots from two independent sources: the
 * {@code data/&lt;namespace&gt;/curios/entities/*.json} data files, and the legacy IMC registration
 * which Curios unconditionally hands to players. Some large packs do not read the data files from
 * the jars on every launch - they replay a cached copy of them - so a slot that only exists in the
 * data files can silently keep the values of an older build. Registering through IMC as well makes
 * both slots appear on the player regardless of that cache.
 *
 * <p>The data files are still shipped, because IMC cannot express the {@code curios:all} validator
 * that makes a slot accept any item - that part only exists in the JSON.
 */
public final class QuickUseSlots {

    /** Plain slot, used by the normal hotkey. */
    public static final String SLOT_ID = "quick_use";

    /** Slot whose use behaves as if shift were held, used by the second hotkey. */
    public static final String SNEAK_SLOT_ID = "quick_use_sneak";

    /** Puts both slots at the very end of the Curios list. */
    private static final int ORDER = 10000;

    @SuppressWarnings({"deprecation", "removal"})
    public static void register() {
        announce(SLOT_ID, "slot/quick_use", ORDER);
        announce(SNEAK_SLOT_ID, "slot/quick_use_sneak", ORDER + 1);
    }

    @SuppressWarnings({"deprecation", "removal"})
    private static void announce(String identifier, String icon, int order) {
        InterModComms.sendTo("curios", SlotTypeMessage.REGISTER_TYPE,
                () -> new SlotTypeMessage.Builder(identifier).size(1).priority(order)
                        .icon(QuickUseSlot.id(icon)).build());
    }

    private QuickUseSlots() {
    }
}
