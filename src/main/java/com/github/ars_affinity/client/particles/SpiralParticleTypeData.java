package com.github.ars_affinity.client.particles;

import com.hollingsworth.arsnouveau.client.particle.ParticleColor;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.network.RegistryFriendlyByteBuf;

public class SpiralParticleTypeData implements ParticleOptions {

    protected ParticleType<? extends SpiralParticleTypeData> type;
    public static final MapCodec<SpiralParticleTypeData> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
                    Codec.FLOAT.fieldOf("r").forGetter(d -> d.color.getRed()),
                    Codec.FLOAT.fieldOf("g").forGetter(d -> d.color.getGreen()),
                    Codec.FLOAT.fieldOf("b").forGetter(d -> d.color.getBlue()),
                    Codec.BOOL.fieldOf("disableDepthTest").forGetter(d -> d.disableDepthTest),
                    Codec.FLOAT.fieldOf("size").forGetter(d -> d.size),
                    Codec.FLOAT.fieldOf("alpha").forGetter(d -> d.alpha),
                    Codec.INT.fieldOf("age").forGetter(d -> d.age),
                    Codec.STRING.fieldOf("spriteType").forGetter(d -> d.spriteType)
            )
            .apply(instance, SpiralParticleTypeData::new));

    public static final StreamCodec<RegistryFriendlyByteBuf, SpiralParticleTypeData> STREAM_CODEC = StreamCodec.of(
            SpiralParticleTypeData::toNetwork, SpiralParticleTypeData::fromNetwork
    );

    public ParticleColor color;
    public boolean disableDepthTest;
    public float size = .25f;
    public float alpha = 1.0f;
    public int age = 36;
    public String spriteType = "default";
    public int playerId = 0;
    public String schoolId = "default";

    public SpiralParticleTypeData(float r, float g, float b, boolean disableDepthTest, float size, float alpha, int age) {
        this(null, new ParticleColor(r, g, b), disableDepthTest, size, alpha, age, "default");
    }
    
    public SpiralParticleTypeData(float r, float g, float b, boolean disableDepthTest, float size, float alpha, int age, String spriteType) {
        this(null, new ParticleColor(r, g, b), disableDepthTest, size, alpha, age, spriteType);
    }

    public SpiralParticleTypeData(ParticleColor color, boolean disableDepthTest, float size, float alpha, int age) {
        this(null, color, disableDepthTest, size, alpha, age, "default");
    }

    public SpiralParticleTypeData(ParticleColor color, boolean disableDepthTest, float size, float alpha, int age, String spriteType) {
        this(null, color, disableDepthTest, size, alpha, age, spriteType);
    }

    public SpiralParticleTypeData(ParticleType<? extends SpiralParticleTypeData> particleTypeData, ParticleColor color, boolean disableDepthTest) {
        this(particleTypeData, color, disableDepthTest, 0.1f, 1.0f, 80, "default");
    }

    public SpiralParticleTypeData(ParticleType<? extends SpiralParticleTypeData> particleTypeData, ParticleColor color, boolean disableDepthTest, float size, float alpha, int age) {
        this(particleTypeData, color, disableDepthTest, size, alpha, age, "default");
    }

    public SpiralParticleTypeData(ParticleType<? extends SpiralParticleTypeData> particleTypeData, ParticleColor color, boolean disableDepthTest, float size, float alpha, int age, String spriteType) {
        this.type = particleTypeData;
        this.color = color;
        this.disableDepthTest = disableDepthTest;
        this.size = size;
        this.alpha = alpha;
        this.age = age;
        this.spriteType = spriteType;
        this.playerId = 0;
        this.schoolId = "default";
    }
    
    public SpiralParticleTypeData(ParticleType<? extends SpiralParticleTypeData> particleTypeData, ParticleColor color, boolean disableDepthTest, float size, float alpha, int age, String spriteType, int playerId, String schoolId) {
        this.type = particleTypeData;
        this.color = color;
        this.disableDepthTest = disableDepthTest;
        this.size = size;
        this.alpha = alpha;
        this.age = age;
        this.spriteType = spriteType;
        this.playerId = playerId;
        this.schoolId = schoolId;
    }
    
    public void setType(ParticleType<? extends SpiralParticleTypeData> type) {
        this.type = type;
    }

    @Override
    public ParticleType<? extends SpiralParticleTypeData> getType() {
        return type;
    }

    public static void toNetwork(RegistryFriendlyByteBuf buf, SpiralParticleTypeData data) {
        buf.writeFloat(data.color.getRed());
        buf.writeFloat(data.color.getGreen());
        buf.writeFloat(data.color.getBlue());
        buf.writeBoolean(data.disableDepthTest);
        buf.writeFloat(data.size);
        buf.writeFloat(data.alpha);
        buf.writeInt(data.age);
        buf.writeUtf(data.spriteType);
    }

    public static SpiralParticleTypeData fromNetwork(RegistryFriendlyByteBuf buffer) {
        float r = buffer.readFloat();
        float g = buffer.readFloat();
        float b = buffer.readFloat();
        boolean disableDepthTest = buffer.readBoolean();
        float size = buffer.readFloat();
        float alpha = buffer.readFloat();
        int age = buffer.readInt();
        String spriteType = buffer.readUtf();
        return new SpiralParticleTypeData(r, g, b, disableDepthTest, size, alpha, age, spriteType);
    }
}
