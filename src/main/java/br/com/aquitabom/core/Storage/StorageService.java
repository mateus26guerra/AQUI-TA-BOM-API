package br.com.aquitabom.core.Storage;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;

import javax.imageio.ImageIO;
import javax.imageio.ImageWriter;
import javax.imageio.ImageWriteParam;
import javax.imageio.stream.ImageOutputStream;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.util.Iterator;
import java.util.UUID;

@Service
public class StorageService {

    private final S3Client s3Client;

    @Value("${r2.bucket}")
    private String bucket;

    @Value("${r2.public-url}")
    private String publicUrl;

    public StorageService(S3Client s3Client) {
        this.s3Client = s3Client;
    }

    public String upload(MultipartFile file) throws Exception {

        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("Imagem é obrigatória");
        }

        if (file.getContentType() == null || !file.getContentType().startsWith("image/")) {
            throw new IllegalArgumentException("Arquivo enviado não é uma imagem");
        }

        BufferedImage original = ImageIO.read(file.getInputStream());

        if (original == null) {
            throw new IllegalArgumentException("Imagem inválida");
        }

        BufferedImage imagemTratada = redimensionar(original, 1200);

        String contentType;
        String extensao;
        byte[] imagemBytes;

        if (existeWriterWebp()) {
            imagemBytes = converterParaWebp(imagemTratada, 0.80f);
            contentType = "image/webp";
            extensao = "webp";
        } else {
            imagemBytes = converterParaJpeg(imagemTratada, 0.80f);
            contentType = "image/jpeg";
            extensao = "jpg";
        }

        String key = "produtos/" + UUID.randomUUID() + "." + extensao;

        s3Client.putObject(
                b -> b.bucket(bucket)
                        .key(key)
                        .contentType(contentType),
                RequestBody.fromBytes(imagemBytes)
        );

        return publicUrl + "/" + key;
    }

    public void deletar(String imagemUrl) {

        if (imagemUrl == null || imagemUrl.isBlank()) {
            return;
        }

        String key = extrairKey(imagemUrl);

        if (key == null) {
            return;
        }

        try {
            s3Client.deleteObject(b -> b.bucket(bucket).key(key));
        } catch (Exception e) {
            System.out.println("Falha ao deletar imagem antiga do bucket: " + e.getMessage());
        }
    }

    private String extrairKey(String imagemUrl) {

        String prefixo = publicUrl + "/";

        if (!imagemUrl.startsWith(prefixo)) {
            return null;
        }

        return imagemUrl.substring(prefixo.length());
    }

    private BufferedImage redimensionar(BufferedImage original, int tamanhoMaximo) {

        int larguraOriginal = original.getWidth();
        int alturaOriginal = original.getHeight();

        if (larguraOriginal <= tamanhoMaximo && alturaOriginal <= tamanhoMaximo) {
            return converterParaRgb(original);
        }

        double proporcao = Math.min(
                (double) tamanhoMaximo / larguraOriginal,
                (double) tamanhoMaximo / alturaOriginal
        );

        int novaLargura = (int) Math.round(larguraOriginal * proporcao);
        int novaAltura = (int) Math.round(alturaOriginal * proporcao);

        BufferedImage novaImagem = new BufferedImage(
                novaLargura,
                novaAltura,
                BufferedImage.TYPE_INT_RGB
        );

        Graphics2D g = novaImagem.createGraphics();
        g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
        g.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        g.drawImage(original, 0, 0, novaLargura, novaAltura, Color.WHITE, null);
        g.dispose();

        return novaImagem;
    }

    private BufferedImage converterParaRgb(BufferedImage original) {

        BufferedImage novaImagem = new BufferedImage(
                original.getWidth(),
                original.getHeight(),
                BufferedImage.TYPE_INT_RGB
        );

        Graphics2D g = novaImagem.createGraphics();
        g.drawImage(original, 0, 0, Color.WHITE, null);
        g.dispose();

        return novaImagem;
    }

    private boolean existeWriterWebp() {
        ImageIO.scanForPlugins();

        Iterator<ImageWriter> writers = ImageIO.getImageWritersByMIMEType("image/webp");
        if (writers.hasNext()) {
            return true;
        }

        Iterator<ImageWriter> formatWriters = ImageIO.getImageWritersByFormatName("webp");
        return formatWriters.hasNext();
    }

    private byte[] converterParaWebp(BufferedImage image, float qualidade) throws Exception {
        return escreverImagem(image, "image/webp", "webp", "Lossy", qualidade);
    }

    private byte[] converterParaJpeg(BufferedImage image, float qualidade) throws Exception {
        return escreverImagem(image, "image/jpeg", "jpg", "JPEG", qualidade);
    }

    private byte[] escreverImagem(
            BufferedImage image,
            String mimeType,
            String formatName,
            String compressionType,
            float qualidade
    ) throws Exception {

        ByteArrayOutputStream output = new ByteArrayOutputStream();
        ImageWriter writer = obterWriter(mimeType, formatName);

        if (writer == null) {
            throw new IllegalStateException("Nenhum writer suportado encontrado para " + mimeType);
        }

        try (ImageOutputStream ios = ImageIO.createImageOutputStream(output)) {
            writer.setOutput(ios);

            ImageWriteParam param = writer.getDefaultWriteParam();

            if (param.canWriteCompressed()) {
                param.setCompressionMode(ImageWriteParam.MODE_EXPLICIT);
                param.setCompressionType(compressionType);
                param.setCompressionQuality(qualidade);
            }

            writer.write(null, new javax.imageio.IIOImage(image, null, null), param);
        } finally {
            writer.dispose();
        }

        return output.toByteArray();
    }

    private ImageWriter obterWriter(String mimeType, String formatName) {
        ImageIO.scanForPlugins();

        Iterator<ImageWriter> writers = ImageIO.getImageWritersByMIMEType(mimeType);

        if (!writers.hasNext()) {
            writers = ImageIO.getImageWritersByFormatName(formatName);
        }

        return writers.hasNext() ? writers.next() : null;
    }
}