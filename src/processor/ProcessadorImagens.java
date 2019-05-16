package processor;

import java.awt.image.BufferedImage;

/**
 *
 * @author ruangomes
 */
public class ProcessadorImagens {

    public BufferedImage brilho(BufferedImage img, int fator) {
        //cria imagem de saída com mesmo tamanho da imagem de entrada
        for (int i = 0; i < img.getWidth(); i++) {
            for (int j = 0; j < img.getHeight(); j++) {

                //acessa o valor do pixel (R, G, B)
                int pixel = img.getRGB(i, j);

                //separa os componentes de cor
                int R = (pixel >> 16) & 255;
                int G = (pixel >> 8) & 255;
                int B = pixel & 255;

                //aumenta o valor de cada componente de cor de acordo com o fator passado como parâmetro
                R = R + fator;
                G = G + fator;
                B = B + fator;

                //limita os valores de R, G e B a 255 (valor máximo)
                if (R > 255) {
                    R = 255;
                }
                if (G > 255) {
                    G = 255;
                }
                if (B > 255) {
                    B = 255;
                }

                if (R < 0) {
                    R = 0;
                }
                if(G < 0) {
                    G = 0;
                }
                if (B < 0) {
                    B = 0;
                }

                //junta os valores de RGB
                int pixel_out = (R << 16) | (G << 8) | B;

                //atribui novo valor de pixel na imagem de saída
                img.setRGB(i, j, pixel_out);
            }
        }

        return img;
    }

    public void brilho_intervalo(BufferedImage img, int fator, int limite, BufferedImage img_out) {

        //verifica se o limite passado é maior que o comprimento da imagem
        if (limite > img.getWidth()) {
            limite = img.getWidth();
        }

        //o primeiro for agora só percorre até o limite
        for (int i = 0; i < limite; i++) {
            for (int j = 0; j < img.getHeight(); j++) {

                //acessa o valor do pixel (R, G, B)
                int pixel = img.getRGB(i, j);

                //separa os componentes de cor
                int R = (pixel >> 16) & 255;
                int G = (pixel >> 8) & 255;
                int B = pixel & 255;

                //aumenta o valor de cada componente de cor de acordo com o fator passado como parâmetro
                R = R + fator;
                G = G + fator;
                B = B + fator;

                //limita os valores de R, G e B a 255 (valor máximo)
                if (R > 255) {
                    R = 255;
                }
                if (G > 255) {
                    G = 255;
                }
                if (B > 255) {
                    B = 255;
                }

                if (R < 0) {
                    R = 0;
                }
                if(G < 0) {
                    G = 0;
                }
                if (B < 0) {
                    B = 0;
                }


                //junta os valores de RGB
                int pixel_out = (R << 16) | (G << 8) | B;

                //atribui novo valor de pixel na imagem de saída
                img_out.setRGB(i, j, pixel_out);
            }
        }
    }
}
