package com.example.examplemod.skill.blessing;

/**
 * How a forged ability should interpret Double Trigger.
 */
public enum DoubleTriggerType {
    /** Run or extend the active action as a second cast/result. */
    DOUBLE_SKILL,
    /** Duplicate produced items/blocks/resources/results. */
    DOUBLE_RESULT,
    /** Duplicate or extend a timed/status/damage effect. */
    DOUBLE_EFFECT,
    /** Passive/continuous/toggle/storage effects that should not be duplicated. */
    NO_DOUBLE,
    /** Passive part stays normal while only the active part can double. */
    HYBRID,
    /** Effect needs a custom rule instead of generic replay. */
    SPECIAL
}
