package lidwinae.clean.and.clear.glass.client;

import net.minecraft.client.model.geom.builders.UVPair;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.BlockModelPart;
import net.minecraft.client.renderer.block.model.SimpleModelWrapper;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.QuadCollection;
import net.minecraft.core.Direction;
import net.neoforged.neoforge.client.model.quad.BakedColors;
import net.neoforged.neoforge.client.model.quad.BakedNormals;
import org.joml.Vector3f;

import java.util.Objects;

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

    private final QuadCollection.Builder builder;

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

    NeoQuadEmitter(QuadCollection.Builder builder) {
        this.builder = builder;
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

        BakedQuad quad = new BakedQuad(
                new Vector3f(positions[0]),
                new Vector3f(positions[1]),
                new Vector3f(positions[2]),
                new Vector3f(positions[3]),
                packedUv(usedSprite, 0),
                packedUv(usedSprite, 1),
                packedUv(usedSprite, 2),
                packedUv(usedSprite, 3),
                -1,
                face,
                usedSprite,
                shade,
                0,
                BakedNormals.UNSPECIFIED,
                BakedColors.DEFAULT,
                false
        );

        if (cullFace == null) {
            builder.addUnculledFace(quad);
        } else {
            builder.addCulledFace(cullFace, quad);
        }

        clear();
        return this;
    }

    BlockModelPart buildPart(TextureAtlasSprite particleSprite) {
        return new SimpleModelWrapper(
                builder.build(),
                false,
                particleSprite,
                null
        );
    }

    private long packedUv(
            TextureAtlasSprite sprite,
            int vertex
    ) {
        return UVPair.pack(
                sprite.getU(u[vertex]),
                sprite.getV(v[vertex])
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
}