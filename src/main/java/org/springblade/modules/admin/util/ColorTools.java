package org.springblade.modules.admin.util;

import lombok.extern.slf4j.Slf4j;

import java.awt.*;

@Slf4j
public class ColorTools {


	public static int getH(Color color){

		float H = -1;
		float R,G,B;

		R = color.getRed() / 255f;
		G = color.getGreen() / 255f;
		B = color.getBlue() / 255f;

		log.info("R,G,B:{},{},{}",R,G,B);

		float min = Math.min(R, Math.min(G, B));
		float max = Math.max(R, Math.max(G, B));

		if(min == max){
			return 0;
		}

		if(max == R){
			H = 60 * (G - B) / (max - min);
			if(G < B){
				H = H + 360;
			}
		}

		if(max == G){
			H = 60 * (B - R) / (max - min) + 120;
		}

		if(max == B){
			H = 60 * (R - G) / (max - min) + 240;
		}

		return Math.round(H);
	}

	public static void main(String[] args) {
		Color color = new Color(147,205,221);
		System.out.println(getH(color));
	}
}
