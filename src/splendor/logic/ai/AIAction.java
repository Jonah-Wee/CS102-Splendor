package splendor.logic.ai;

import java.util.List;
import splendor.entities.GemColor;
import splendor.entities.Tier;

/**
 * Represents a fully-specified action chosen by the AI.
 * The ActionType determines which fields are relevant.
 *
 * TAKE_GEMS   → gems (1–3 colours)
 * BUY_CARD    → tier + slotIndex  (or reservedIndex if buying from hand)
 * RESERVE_CARD → tier + slotIndex (slotIndex = -1 means top of deck)
 */
public record AIAction(
        ActionType type,

        // Used by TAKE_GEMS
        List<GemColor> gems,

        // Used by BUY_CARD and RESERVE_CARD
        Tier tier,
        int slotIndex,

        // Used by BUY_CARD when buying from reserved hand
        boolean buyingReserved,
        int reservedIndex
) {

    public enum ActionType {
        TAKE_GEMS,
        BUY_CARD,
        RESERVE_CARD
    }

    
    /** Take up to three different gems (or two of the same colour). */
    public static AIAction takeGems(List<GemColor> gems) {
        return new AIAction(ActionType.TAKE_GEMS, List.copyOf(gems),
                null, -1, false, -1);
    }

    /** Buy a face-up card on the board. */
    public static AIAction buyVisible(Tier tier, int slotIndex) {
        return new AIAction(ActionType.BUY_CARD, List.of(),
                tier, slotIndex, false, -1);
    }

    /** Buy a card from the AI's own reserved hand. */
    public static AIAction buyReserved(int reservedIndex) {
        return new AIAction(ActionType.BUY_CARD, List.of(),
                null, -1, true, reservedIndex);
    }

    /** Reserve a face-up card. */
    public static AIAction reserveVisible(Tier tier, int slotIndex) {
        return new AIAction(ActionType.RESERVE_CARD, List.of(),
                tier, slotIndex, false, -1);
    }

    /** Reserve the top card of a deck (slotIndex sentinel = -1). */
    public static AIAction reserveTopOfDeck(Tier tier) {
        return new AIAction(ActionType.RESERVE_CARD, List.of(),
                tier, -1, false, -1);
    }

    @Override
    public String toString() {
        return switch (type) {
            case TAKE_GEMS     -> "Take gems: " + gems;
            case BUY_CARD      -> buyingReserved
                    ? "Buy reserved card #" + (reservedIndex + 1)
                    : "Buy card [tier=" + tier + ", slot=" + (slotIndex + 1) + "]";
            case RESERVE_CARD  -> slotIndex == -1
                    ? "Reserve top of deck [tier=" + tier + "]"
                    : "Reserve card [tier=" + tier + ", slot=" + (slotIndex + 1) + "]";
        };
    }
}
