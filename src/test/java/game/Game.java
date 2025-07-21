package game;

import me.redstoner2019.audio.SoundProvider;
import me.redstoner2019.graphics.RenderI;
import me.redstoner2019.graphics.font.TextRenderer;
import me.redstoner2019.graphics.mesh.Mesh;
import me.redstoner2019.graphics.mesh.Rectangle;
import me.redstoner2019.graphics.mesh.Vertex2D;
import me.redstoner2019.graphics.render.Renderer;
import me.redstoner2019.graphics.texture.TextureProvider;
import me.redstoner2019.gui.events.KeyPressedEvent;
import me.redstoner2019.gui.window.Window;
import me.redstoner2019.threed.render.Renderer3D;
import me.redstoner2019.util.Resources;
import org.lwjgl.glfw.GLFW;

import java.awt.*;
import java.util.HashMap;

import static java.lang.Math.sin;

public class Game extends Window {

    private float x;
    private float y = 0;
    private float vx = 0;
    private float vy = 0;
    private HashMap<String,String> world = new HashMap<>();
    private HashMap<Integer,Integer> highestBlockAt = new HashMap<>();
    private HashMap<String,Mesh> chunks = new HashMap<>();

    public Game(float wx, float wy, float width, float height) {
        super(wx, wy, width, height);

        init();

        TextureProvider textureProvider = TextureProvider.getInstance();
        SoundProvider soundProvider = SoundProvider.getInstance();

        for(String s : Resources.listResources("audio")){
            soundProvider.loadSound(s);
        }

        for(String s : Resources.listResources("textures")){
            System.out.println("Loading " + s);
            textureProvider.loadTexture(s);
        }

        addKeyPressedEvent(new KeyPressedEvent() {
            @Override
            public void keyPressedEvent(int key, int action, int mods) {
                if(key == GLFW.GLFW_KEY_ESCAPE && action == GLFW.GLFW_RELEASE){
                    System.exit(0);
                }
            }
        });

        addRenderer(new RenderI() {
            @Override
            public void render(Renderer renderer, Renderer3D renderer3D, TextRenderer textRenderer) {
                int renderDistance = 5;
                int chunkCount = 0;

                for (int chunkX = -renderDistance; chunkX < renderDistance; chunkX++) {
                    for (int chunkY = -renderDistance; chunkY < renderDistance; chunkY++) {
                        if(false) continue;
                        if(chunks.containsKey(chunkX + " " + chunkY)){
                            Mesh m = chunks.get(chunkX + " " + chunkY);
                            if(m == null) continue;
                            renderer.renderMesh(TextureProvider.getInstance().get("textures.Ground.ground2.png"),m);
                            chunkCount++;
                            continue;
                        }
                        /*int finalChunkX = chunkX;
                        int finalChunkY = chunkY;
                        chunks.put(finalChunkX + " " + finalChunkY, null);
                        Thread t = new Thread(new Runnable() {
                            @Override
                            public void run() {
                                int chunkSize = 10;
                                Mesh m = new Mesh();
                                for (int x = 0; x < chunkSize; x++) {
                                    for (int y = 0; y < chunkSize; y++) {
                                        String type = world.getOrDefault(x + " " + y,"air");
                                        if(type.equals("air")) continue;
                                        m.addRectangle(Rectangle.ofCenter(new Vertex2D(x + (finalChunkX * chunkSize),y + (finalChunkY * chunkSize)),1));
                                    }
                                }
                                m.setScalingX(getAspectRatio());
                                m.setOffsetX(finalChunkX);
                                m.setOffsetY(finalChunkY);
                                m.bake();
                                System.out.println("Bake done");
                                chunks.put(finalChunkX + " " + finalChunkY, m);
                            }
                        });
                        t.start();*/
                        Mesh m = new Mesh();
                        m.addRectangle(Rectangle.ofCenter(new Vertex2D(0,0),1));
                        m.setOffsetY(chunkY);
                        m.setOffsetY(chunkX);
                        m.bake();
                        chunks.put(chunkX + " " + chunkY, m);
                    }
                }
                /*int blockX = (int) x;
                int blockY = (int) y;
                float blockSize = .2f;
                int renderDistance = (int) (2/blockSize);
                float r = getAspectRatio();

                for (int bx = -renderDistance; bx < renderDistance; bx++) {
                    for (int by = -renderDistance; by < renderDistance; by++) {
                        int px = bx + blockX;
                        int py = by + blockY;

                        float rx = (px / r) * blockSize - (blockSize / r / 2f);
                        float ry = py * blockSize - (blockSize / 2f);

                        String type = world.getOrDefault(px + " " + py,"air");

                        if(type.equals("air")) continue;

                        renderer.renderTexture(rx - (x * blockSize / r),ry - y * blockSize,blockSize / r,blockSize, TextureProvider.getInstance().get("textures.Ground.ground2.png"));
                    }
                }*/

                //textRenderer.renderText(getFps() + " FPS",0,0,20, Color.WHITE);
                //textRenderer.renderText(String.format("Position: %.2f / %.2f", x, y),0,20,20, Color.WHITE);
                //textRenderer.renderText(GLFW.glfwGetTime() + "",0,40,20, Color.WHITE);
                //textRenderer.renderText("Chunks: " + chunkCount,0,60,20, Color.WHITE);
                //textRenderer.renderText(m.getVertices().length + " vertices",0,60,20, Color.WHITE);
                //textRenderer.renderText((m.getIndices().length / 3) + " triangles",0,80,20, Color.WHITE);
            }
        });


        Thread t = new Thread(new Runnable() {
            @Override
            public void run() {

                FastNoiseLite fnl = new FastNoiseLite();
                fnl.SetFrequency(0.001f);
                fnl.SetFractalType(FastNoiseLite.FractalType.FBm);

                while (true) {
                    int blockX = (int) x;
                    int blockY = (int) y;

                    int d = 500;

                    for (int bx = -d; bx < d; bx++) {
                        for (int by = -d; by < d; by++) {
                            int x0 = blockX + bx;
                            int y0 = blockY + by;
                            if(!world.containsKey(x0 + " " + y0)){
                                int h = (int) (fnl.GetNoise(x0,0) * 255);
                                highestBlockAt.put(x0,h);
                                if(h > y0) {
                                    world.put(x0 + " " + y0,"block");
                                } else {
                                    world.put(x0 + " " + y0,"air");
                                }
                            }
                        }
                    }
                }
            }
        });
        t.start();

        loop();
    }

    @Override
    public void update(float deltaTime) {
        float speed = .1f;

        if(isKeyDown(GLFW.GLFW_KEY_LEFT_SHIFT)) {
            speed = speed * 10;
        }
        if(isKeyDown(GLFW.GLFW_KEY_A)) {
            this.x += speed * deltaTime;
        }
        if(isKeyDown(GLFW.GLFW_KEY_D)) {
            this.x += -speed * deltaTime;
        }
        if(isKeyDown(GLFW.GLFW_KEY_W)) {
            this.y += -speed * deltaTime;
        }
        if(isKeyDown(GLFW.GLFW_KEY_S)) {
            this.y += speed * deltaTime;
        }

        /*if(isKeyDown(GLFW.GLFW_KEY_LEFT_SHIFT)) {
            speed = speed * 10;
        }
        if(isKeyDown(GLFW.GLFW_KEY_A)) {
            this.vx = -speed;
        }
        if(isKeyDown(GLFW.GLFW_KEY_D)) {
            this.vx = speed;
        }
        if(isKeyDown(GLFW.GLFW_KEY_SPACE)) {
            this.vy = speed;
        }
        this.x+=this.vx*deltaTime;
        this.y+=this.vy*deltaTime;
        vx*=0.999f*deltaTime;
        vy-=0.01f*deltaTime;

        int bx = (int) x;
        int by = (int) y;

        String blockAt = world.getOrDefault(bx + " " + by,"air");

        if(!blockAt.equals("air")){
            vy = 0;
            y = highestBlockAt.get(bx) - 1;
        }*/
    }

    public static void main(String[] args) {
        new Game(0,0,1280,720);
    }
}
