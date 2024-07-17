package me.redstoner2019.gui.events;

public interface KeyPressedEvent {
    /**
     *
     * @param key Key Pressed. Use for example GLFW_KEY_SPACE for the keys.
     * @param action Action, wether the key was GLFW_PRESS or GLFW_RELEASE
     * @param mods Actions mods
     */
    void keyPressedEvent(int key, int action, int mods);
}
