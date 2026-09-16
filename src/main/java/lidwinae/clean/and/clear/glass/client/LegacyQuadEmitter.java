package lidwinae.clean.and.clear.glass.client;

import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.core.Direction;
import net.neoforged.neoforge.client.ClientHooks;
import org.joml.Vector3f;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Objects;

/**
 * Small quad builder for Minecraft 1.21.1's legacy baked-quad format.
 *
 * <p>The connected-model geometry uses normalized block coordinates and
 * normalized sprite coordinates. This class converts those values to the
 * packed 32-integer vertex layout expected by {@link BakedQuad}.</p>
 */
final class NeoQuadEmitter {
    static final int BAKE_ROTATE_NONE = 0;
    static final int BAKE_ROTATE_90 = 1;
    static final int BAKE_ROTATE_180 = 2;
    static final int BAKE_ROTATE_270 = 3;
    static final int BAKE_LOCK_UV = 4;
    static final int BAKE_FLIP_U = 8;
    static final int BAKE_FLIP_V = 16;
    static final int BAKE_NORMALIZED = 32;

    private static final float CULL_FACE_EPSILON = 0.00001F;

    private final List<BakedQuad> unculled = new ArrayList<>();
    private final EnumMap<Direction, List<BakedQuad>> culled =
            new EnumMap<>(Direction.class);

    private final Vector3f[] positions = {
            new Vector3f(),
            new Vector3f(),
            new Vector3f(),
            new Vector3f()
    };

    private final float[] u = new float[4];
    private final float[] v = new float[4];

    private Direction nominalFace;
    private Direction cullFace;
    private TextureAtlasSprite sprite;
    private boolean shade = true;

    NeoQuadEmitter() {
        for (Direction direction : Direction.values()) {
            culled.put(direction, new ArrayList<>());
        }
    }

    NeoQuadEmitter pos(
            int vertex,
            float x,
            float y,
            float z
    ) {
        positions[vertex].set(x, y, z);
        return this;
    }

    NeoQuadEmitter uv(
            int vertex,
            float u,
            float v
    ) {
        this.u[vertex] = u;
        this.v[vertex] = v;
        return this;
    }

    NeoQuadEmitter nominalFace(Direction face) {
        this.nominalFace = face;
        return this;
    }

    NeoQuadEmitter cullFace(Direction face) {
        this.cullFace = face;
        return this;
    }

    NeoQuadEmitter diffuseShade(boolean shade) {
        this.shade = shade;
        return this;
    }

    NeoQuadEmitter square(
            Direction face,
            float left,
            float bottom,
            float right,
            float top,
            float depth
    ) {
        if (Math.abs(depth) < CULL_FACE_EPSILON) {
            cullFace(face);
            depth = 0;
        } else {
            cullFace(null);
        }

        nominalFace(face);

        switch (face) {
            case UP -> {
                depth = 1 - depth;
                top = 1 - top;
                bottom = 1 - bottom;

                pos(0, left, depth, top);
                pos(1, left, depth, bottom);
                pos(2, right, depth, bottom);
                pos(3, right, depth, top);
            }
            case DOWN -> {
                pos(0, left, depth, top);
                pos(1, left, depth, bottom);
                pos(2, right, depth, bottom);
                pos(3, right, depth, top);
            }
            case EAST -> {
                depth = 1 - depth;
                left = 1 - left;
                right = 1 - right;

                pos(0, depth, top, left);
                pos(1, depth, bottom, left);
                pos(2, depth, bottom, right);
                pos(3, depth, top, right);
            }
            case WEST -> {
                pos(0, depth, top, left);
                pos(1, depth, bottom, left);
                pos(2, depth, bottom, right);
                pos(3, depth, top, right);
            }
            case SOUTH -> {
                depth = 1 - depth;
                left = 1 - left;
                right = 1 - right;

                pos(0, 1 - left, top, depth);
                pos(1, 1 - left, bottom, depth);
                pos(2, 1 - right, bottom, depth);
                pos(3, 1 - right, top, depth);
            }
            case NORTH -> {
                pos(0, 1 - left, top, depth);
                pos(1, 1 - left, bottom, depth);
                pos(2, 1 - right, bottom, depth);
                pos(3, 1 - right, top, depth);
            }
        }

        return this;
    }

    NeoQuadEmitter spriteBake(
            TextureAtlasSprite sprite,
            int flags
    ) {
        this.sprite = sprite;

        if ((flags & BAKE_LOCK_UV) != 0) {
            applyLockedUv();
        } else if ((flags & BAKE_NORMALIZED) == 0) {
            for (int i = 0; i < 4; i++) {
                u[i] /= 16.0F;
                v[i] /= 16.0F;
            }
        }

        int rotation = flags & 3;

        for (int i = 0; i < 4; i++) {
            float oldU = u[i];
            float oldV = v[i];

            switch (rotation) {
                case BAKE_ROTATE_90 -> {
                    u[i] = oldV;
                    v[i] = 1 - oldU;
                }
                case BAKE_ROTATE_180 -> {
                    u[i] = 1 - oldU;
                    v[i] = 1 - oldV;
                }
                case BAKE_ROTATE_270 -> {
                    u[i] = 1 - oldV;
                    v[i] = oldU;
                }
                default -> {
                }
            }

            if ((flags & BAKE_FLIP_U) != 0) {
                u[i] = 1 - u[i];
            }

            if ((flags & BAKE_FLIP_V) != 0) {
                v[i] = 1 - v[i];
            }
        }

        return this;
    }

    NeoQuadEmitter emit() {
        Direction face = Objects.requireNonNull(
                nominalFace,
                "Quad has no nominal face"
        );

        TextureAtlasSprite usedSprite = Objects.requireNonNull(
                sprite,
                "Quad has no texture sprite"
        );

        int[] vertices = new int[32];

        for (int vertex = 0; vertex < 4; vertex++) {
            int offset = vertex * 8;
            Vector3f position = positions[vertex];

            vertices[offset] =
                    Float.floatToRawIntBits(position.x());
            vertices[offset + 1] =
                    Float.floatToRawIntBits(position.y());
            vertices[offset + 2] =
                    Float.floatToRawIntBits(position.z());
            vertices[offset + 3] = -1;
            vertices[offset + 4] =
                    Float.floatToRawIntBits(
                            usedSprite.getU(u[vertex])
                    );
            vertices[offset + 5] =
                    Float.floatToRawIntBits(
                            usedSprite.getV(v[vertex])
                    );
            vertices[offset + 6] = 0;
        }

        ClientHooks.fillNormal(vertices, face);

        BakedQuad quad = new BakedQuad(
                vertices,
                -1,
                face,
                usedSprite,
                shade,
                false
        );

        if (cullFace == null) {
            unculled.add(quad);
        } else {
            culled.get(cullFace).add(quad);
        }

        clear();
        return this;
    }

    QuadSet build() {
        EnumMap<Direction, List<BakedQuad>> immutableCulled =
                new EnumMap<>(Direction.class);

        for (Direction direction : Direction.values()) {
            immutableCulled.put(
                    direction,
                    List.copyOf(culled.get(direction))
            );
        }

        return new QuadSet(
                List.copyOf(unculled),
                immutableCulled
        );
    }

    private void applyLockedUv() {
        Direction face = Objects.requireNonNull(nominalFace);

        for (int i = 0; i < 4; i++) {
            Vector3f position = positions[i];

            switch (face) {
                case DOWN -> {
                    u[i] = position.x();
                    v[i] = 1 - position.z();
                }
                case UP -> {
                    u[i] = position.x();
                    v[i] = position.z();
                }
                case NORTH -> {
                    u[i] = 1 - position.x();
                    v[i] = 1 - position.y();
                }
                case SOUTH -> {
                    u[i] = position.x();
                    v[i] = 1 - position.y();
                }
                case WEST -> {
                    u[i] = position.z();
                    v[i] = 1 - position.y();
                }
                case EAST -> {
                    u[i] = 1 - position.z();
                    v[i] = 1 - position.y();
                }
            }
        }
    }

    private void clear() {
        for (int i = 0; i < 4; i++) {
            positions[i].set(0, 0, 0);
            u[i] = 0;
            v[i] = 0;
        }

        nominalFace = null;
        cullFace = null;
        sprite = null;
        shade = true;
    }

    record QuadSet(
            List<BakedQuad> unculled,
            EnumMap<Direction, List<BakedQuad>> culled
    ) {
        List<BakedQuad> forSide(Direction side) {
            return side == null
                    ? unculled
                    : culled.get(side);
        }
    }
}
