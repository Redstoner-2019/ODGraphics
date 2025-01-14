package me.redstoner2019.gui.window;

import me.redstoner2019.graphics.RenderI;
import me.redstoner2019.graphics.font.TextRenderer;
import me.redstoner2019.util.IOUtil;
import me.redstoner2019.graphics.render.Renderer;
import me.redstoner2019.gui.Component;
import me.redstoner2019.gui.events.KeyPressedEvent;
import me.redstoner2019.gui.events.MouseClickedEvent;
import me.redstoner2019.gui.events.MouseMovedEvent;
import me.redstoner2019.gui.events.ResizeEvent;
import org.lwjgl.glfw.*;
import org.lwjgl.opengl.GL;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL13;
import org.lwjgl.system.MemoryUtil;

import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.stb.STBImage.*;
import static org.lwjgl.stb.STBImage.stbi_image_free;
import static org.lwjgl.system.MemoryUtil.memAllocInt;

public abstract class Window extends Component {

    private long window;
    private boolean vsync;
    private Renderer renderer;
    private TextRenderer textRenderer;
    private boolean fullscreen;
    private float aspectRatio;
    private float deltaTime = 0;
    private long lastUpdate = System.currentTimeMillis();
    private int frames = 0;
    private int fps = 0;
    private double lastFrameTime = 0;
    private int componentsDrawn = 0;
    private String title = "";
    private List<RenderI> renderers = new ArrayList<>();
    private List<MouseClickedEvent> mouseClickedEvents = new ArrayList<>();
    private List<MouseMovedEvent> mouseMovedEvents = new ArrayList<>();
    private List<KeyPressedEvent> keyPressedEvents = new ArrayList<>();
    private List<ResizeEvent> resizeEvents = new ArrayList<>();
    private HashMap<Integer, Boolean> keysPressed = new HashMap<>();

    public Window(float x, float y, float width, float height) {
        super(x, y, width, height);
    }

    public void addRenderer(RenderI renderer){
        renderers.add(renderer);
    }

    public abstract void update(float deltaTime);

    public void addMouseClickEvent(MouseClickedEvent mouseClickedEvent){
        mouseClickedEvents.add(mouseClickedEvent);
    }
    public void addMouseMoveEvent(MouseMovedEvent mouseMovedEvent){
        mouseMovedEvents.add(mouseMovedEvent);
    }
    public void addKeyPressedEvent(KeyPressedEvent keyPressedEvent){
        keyPressedEvents.add(keyPressedEvent);
    }
    public void addResizedEventEvent(ResizeEvent resizeEvent){
        resizeEvents.add(resizeEvent);
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public boolean isVsync() {
        return vsync;
    }

    public void setVsync(boolean vsync) {
        this.vsync = vsync;
    }

    public long getWindow() {
        return window;
    }

    public Renderer getRenderer() {
        return renderer;
    }

    public TextRenderer getTextRenderer() {
        return textRenderer;
    }

    public boolean isFullscreen() {
        return fullscreen;
    }

    public float getAspectRatio() {
        return aspectRatio;
    }

    public float getDeltaTime() {
        return deltaTime;
    }

    public long getLastUpdate() {
        return lastUpdate;
    }

    public int getFps() {
        return fps;
    }

    public double getLastFrameTime() {
        return lastFrameTime;
    }

    public int getComponentsDrawn() {
        return componentsDrawn;
    }

    public void setWindowIcon(String icon16path, String icon32path) {
        ByteBuffer icon16;
        ByteBuffer icon32;
        try {
            icon16 = IOUtil.ioResourceToByteBuffer(icon16path, 2048);
            icon32 = IOUtil.ioResourceToByteBuffer(icon32path, 4096);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }


        IntBuffer w = memAllocInt(1);
        IntBuffer h = memAllocInt(1);
        IntBuffer comp = memAllocInt(1);

        try (GLFWImage.Buffer icons = GLFWImage.malloc(2)) {
            stbi_set_flip_vertically_on_load(false);
            ByteBuffer pixels16 = stbi_load_from_memory(icon16, w, h, comp, 4);
            icons
                    .position(0)
                    .width(w.get(0))
                    .height(h.get(0))
                    .pixels(pixels16);

            stbi_set_flip_vertically_on_load(false);
            ByteBuffer pixels32 = stbi_load_from_memory(icon32, w, h, comp, 4);
            icons
                    .position(1)
                    .width(w.get(0))
                    .height(h.get(0))
                    .pixels(pixels32);

            pixels32.flip();
            pixels16.flip();

            icons.position(0);
            glfwSetWindowIcon(window, icons);

            stbi_image_free(pixels32);
            stbi_image_free(pixels16);
        }
    }

    private void updateProjectionMatrix() {
        GL11.glMatrixMode(GL11.GL_PROJECTION);
        GL11.glLoadIdentity();
        aspectRatio = getWidth() / getHeight();
        GL11.glOrtho(-aspectRatio, aspectRatio, -1f, 1.0f, -1f, 1.0f);
        GL11.glMatrixMode(GL11.GL_MODELVIEW);
        GL11.glLoadIdentity();
    }

    public void toggleFullscreen() {
        fullscreen = !fullscreen;

        GLFWVidMode vidMode = GLFW.glfwGetVideoMode(GLFW.glfwGetPrimaryMonitor());

        if (fullscreen) {
            GLFW.glfwSetWindowMonitor(window, GLFW.glfwGetPrimaryMonitor(), 0, 0, Math.min(vidMode.width(),1920), Math.min(vidMode.height(),1080), vidMode.refreshRate());
        } else {
            GLFW.glfwSetWindowMonitor(window, 0, 50, 50, 1280, 720, 0);
        }
    }

    public void onResize(float newW, float newH){
        for(ResizeEvent e : resizeEvents) e.onNewSize(newW, newH);
    }

    public boolean shouldClose(){
        return GLFW.glfwWindowShouldClose(window);
    }

    public void loop(){
        glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);

        glMatrixMode(GL_PROJECTION);
        glLoadIdentity();
        glOrtho(0, getWidth(), getHeight(), 0, -1, 1);
        glMatrixMode(GL_MODELVIEW);

        GLFW.glfwSwapBuffers(window);
        GLFW.glfwPollEvents();

        while (!shouldClose()) {

            glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);

            glMatrixMode(GL_PROJECTION);
            glLoadIdentity();
            glOrtho(0, getWidth(), getHeight(), 0, -1, 1);
            glMatrixMode(GL_MODELVIEW);

            double start = glfwGetTime();
            deltaTime = (float) (lastFrameTime / (1.0/60.0));

            update(deltaTime);

            glfwSetWindowTitle(window,title);

            for(RenderI renderI : renderers){
                renderI.render(renderer, textRenderer);
            }

            GLFW.glfwSwapBuffers(window);
            GLFW.glfwPollEvents();

            frames++;
            if(System.currentTimeMillis() - lastUpdate >= 1000){
                fps = frames;
                frames = 0;
                lastUpdate = System.currentTimeMillis();
            }
            lastFrameTime = glfwGetTime() - start;
        }

        glfwDestroyWindow(window);
        System.exit(0);
    }

    public void init(){
        GLFWErrorCallback.createPrint(System.err).set();
        if (!GLFW.glfwInit()) {
            throw new IllegalStateException("Unable to initialize GLFW");
        }

        GLFW.glfwDefaultWindowHints();
        GLFW.glfwWindowHint(GLFW.GLFW_VISIBLE, GLFW.GLFW_FALSE);
        GLFW.glfwWindowHint(GLFW.GLFW_RESIZABLE, GLFW.GLFW_TRUE);

        window = GLFW.glfwCreateWindow((int) getWidth(), (int) getHeight(), title, MemoryUtil.NULL, MemoryUtil.NULL);

        if (window == MemoryUtil.NULL) {
            throw new RuntimeException("Failed to create the GLFW window");
        }

        GLFW.glfwMakeContextCurrent(window);

        if(vsync) GLFW.glfwSwapInterval(1);
        else GLFW.glfwSwapInterval(0);

        GLFW.glfwSetFramebufferSizeCallback(window, new GLFWFramebufferSizeCallback() {
            @Override
            public void invoke(long window, int w, int h) {
                onResize(w,h);
                setWidth(w);
                setHeight(h);
                renderer.setHeight(h);
                renderer.setWidth(w);
                GL11.glViewport(0, 0, w, h);
                updateProjectionMatrix();
            }
        });

        GLFW.glfwSetMouseButtonCallback(window, new GLFWMouseButtonCallback() {
            @Override
            public void invoke(long l, int i, int i1, int i2) {
                double[] mouseX = new double[1];
                double[] mouseY = new double[1];
                GLFW.glfwGetCursorPos(window, mouseX, mouseY);
                MouseClickedEvent.Button button = null;
                if(i == 0) button = MouseClickedEvent.Button.LEFT;
                if(i == 1) button = MouseClickedEvent.Button.RIGHT;
                if(i == 2) button = MouseClickedEvent.Button.MIDDLE;
                for(MouseClickedEvent e : mouseClickedEvents){
                    e.onMouseClickedEvent((float) mouseX[0], (float) mouseY[0], (float) (((mouseX[0] / getWidth()) * 2) - 1), (float) -(((mouseY[0] / getHeight()) * 2) - 1), button ,i1 == GLFW_PRESS ? MouseClickedEvent.Type.PRESS : MouseClickedEvent.Type.RELEASE);
                }
            }
        });

        GLFW.glfwSetCursorPosCallback(window, new GLFWCursorPosCallback() {
            @Override
            public void invoke(long window, double xpos, double ypos) {
                for(MouseMovedEvent e : mouseMovedEvents){
                    e.onMouseMovedEvent((float) xpos, (float) ypos, (float) (((xpos / getWidth()) * 2) - 1), (float) -(((ypos / getHeight()) * 2) - 1));
                }
            }
        });

        GLFW.glfwSetKeyCallback(window, new GLFWKeyCallback() {
            @Override
            public void invoke(long l, int i, int i1, int i2, int i3) {
                if(i2 == GLFW.GLFW_RELEASE){
                    keysPressed.put(i,false);
                }

                if(i2 == GLFW_PRESS){
                    keysPressed.put(i,true);
                }

                for(KeyPressedEvent e : keyPressedEvents){
                    e.keyPressedEvent(i,i2,i3);
                }
            }
        });

        GLFW.glfwShowWindow(window);
        GL.createCapabilities();

        GL11.glEnable(GL13.GL_MULTISAMPLE);
        glEnable(GL_BLEND);
        glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);

        updateProjectionMatrix();

        renderer = Renderer.getInstance();
        textRenderer = TextRenderer.getInstance();
    }

    public boolean isKeyDown(int key){
        return keysPressed.getOrDefault(key,false);
    }
}
