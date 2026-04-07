package dev.david.huzzclient.mixin;

import dev.david.huzzclient.HuzzClient;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.network.packet.s2c.play.ChunkDataS2CPacket;
import net.minecraft.network.packet.s2c.play.ChunkDeltaUpdateS2CPacket;
import net.minecraft.network.packet.s2c.play.GameJoinS2CPacket;
import net.minecraft.network.packet.s2c.play.LightUpdateS2CPacket;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ClientPlayNetworkHandler.class)
public abstract class ClientPlayNetworkHandlerMixin {
    @Inject(method = "sendChatMessage", at = @At("HEAD"), cancellable = true)
    private void huzzclient$interceptChatMessage(String content, CallbackInfo ci) {
        if (HuzzClient.getChatCommandController().handleOutgoingMessage(HuzzClient.client(), content)) {
            ci.cancel();
        }
    }

    @Inject(method = "sendChatCommand", at = @At("HEAD"), cancellable = true)
    private void huzzclient$interceptChatCommand(String command, CallbackInfo ci) {
        if (HuzzClient.getChatCommandController().handleOutgoingCommand(HuzzClient.client(), command)) {
            ci.cancel();
        }
    }

    @Inject(method = "onGameJoin", at = @At("TAIL"))
    private void huzzclient$onGameJoin(GameJoinS2CPacket packet, CallbackInfo ci) {
        HuzzClient.handleGameJoin(packet);
    }

    @Inject(method = "onChunkDeltaUpdate", at = @At("TAIL"))
    private void huzzclient$onChunkDeltaUpdate(ChunkDeltaUpdateS2CPacket packet, CallbackInfo ci) {
        HuzzClient.handleChunkDeltaUpdate(packet);
    }

    @Inject(method = "onChunkData", at = @At("TAIL"))
    private void huzzclient$onChunkData(ChunkDataS2CPacket packet, CallbackInfo ci) {
        HuzzClient.handleChunkData(packet);
    }

    @Inject(method = "onLightUpdate", at = @At("TAIL"))
    private void huzzclient$onLightUpdate(LightUpdateS2CPacket packet, CallbackInfo ci) {
        HuzzClient.handleLightUpdate(packet);
    }
}
