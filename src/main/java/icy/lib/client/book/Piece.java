package icy.lib.client.book;

import com.mojang.blaze3d.platform.NativeImage;
import net.minecraft.resources.Identifier;
import net.minecraft.server.packs.resources.Resource;

import java.io.IOException;
import java.io.InputStream;

final class Piece {
    final Identifier texture;
    final int texW;
    final int texH;
    final int x;
    final int y;
    final int w;
    final int h;
    private final long[] solid;

    private Piece(Identifier texture, int texW, int texH, int x, int y, int w, int h, long[] solid) {
        this.texture = texture;
        this.texW = texW;
        this.texH = texH;
        this.x = x;
        this.y = y;
        this.w = w;
        this.h = h;
        this.solid = solid;
    }

    boolean solid(int px, int py) {
        if (px < 0 || py < 0 || px >= texW || py >= texH) {
            return false;
        }
        int i = py * texW + px;
        return (solid[i >>> 6] & (1L << i)) != 0;
    }

    static Piece read(Identifier texture, Resource res) throws IOException {
        try (InputStream in = res.open(); NativeImage img = NativeImage.read(in)) {
            int tw = img.getWidth();
            int th = img.getHeight();
            long[] bits = new long[(tw * th + 63) >>> 6];
            int minX = tw, minY = th, maxX = -1, maxY = -1;
            for (int py = 0; py < th; py++) {
                for (int px = 0; px < tw; px++) {
                    int alpha = img.getPixel(px, py) >>> 24;
                    if (alpha == 0) {
                        continue;
                    }
                    minX = Math.min(minX, px);
                    minY = Math.min(minY, py);
                    maxX = Math.max(maxX, px);
                    maxY = Math.max(maxY, py);
                    if (alpha >= 128) {
                        int i = py * tw + px;
                        bits[i >>> 6] |= 1L << i;
                    }
                }
            }
            if (maxX < 0) {
                return new Piece(texture, tw, th, 0, 0, 0, 0, bits);
            }
            return new Piece(texture, tw, th, minX, minY, maxX - minX + 1, maxY - minY + 1, bits);
        }
    }
}
