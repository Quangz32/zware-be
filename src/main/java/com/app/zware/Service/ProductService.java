package com.app.zware.Service;


import com.app.zware.Entities.Product;
import com.app.zware.Repositories.ProductRepository;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;


@Service
public class ProductService {

  @Autowired
  ProductRepository productRepository;
  @Value("${image.storage.directory}")
  private String storageDirectory;

  public List<Product> getAllProducts() {
    return productRepository.findAll();
  }

  public Product getById(int id) {
    return productRepository.findById(id)
        .orElse(null);
  }

  public Product createProduct(Product request) {
    request.setIsdeleted(false);
    request.setImage(null);
    return productRepository.save(request);
  }

//  public boolean checkIdProductExist(int id) {
//    return productRepository.existsById(id);
//  }

  public void deleteProductById(Integer id) {
    Product product = getById(id);

    product.setIsdeleted(true);
    productRepository.save(product);
  }

  public Product merger(Integer id, Product productRequest) {
    Product oldProduct = getById(id);
    if (oldProduct == null) {
      return null;
    }

    Optional.ofNullable(productRequest.getName()).ifPresent(oldProduct::setName);
    Optional.of(productRequest.getCategory_id()).ifPresent(oldProduct::setCategory_id);
    Optional.ofNullable(productRequest.getSupplier()).ifPresent(oldProduct::setSupplier);
    Optional.ofNullable(productRequest.getMeasure_unit()).ifPresent(oldProduct::setMeasure_unit);

    oldProduct.setIsdeleted(false);
    return oldProduct;
  }

  public Product update(Product product) {
    return productRepository.save(product);
  }

  public String uploadImage(MultipartFile file, Integer productid) throws IOException {
    Product imageData = getById(productid);

    // Delete the old image if it exists
    if (imageData.getImage() != null) {
      Path oldImagePath = Paths.get(storageDirectory, imageData.getImage());
      Files.deleteIfExists(oldImagePath);
    }

    // Create the directory if it doesn't exist
    File directory = new File(storageDirectory);
    if (!directory.exists()) {
      directory.mkdirs();
    }

    // Save the new image with a timestamp in its name
    String timestamp = new SimpleDateFormat("yyyyMMddHHmmss").format(new Date());
    String fileExtension = getFileExtension(file.getOriginalFilename());
    String fileName = timestamp + fileExtension;
    Path filePath = Paths.get(storageDirectory, fileName);
    Files.write(filePath, file.getBytes());

    // Save the new image information to the database
    imageData.setImage(fileName);
    productRepository.save(imageData);

    return "File uploaded successfully: " + fileName;
  }


  public byte[] downloadImage(Integer productid) throws IOException {
    Product product = getById(productid);
    if (product.getImage() != null) {
      Path filePath = Paths.get(storageDirectory, product.getImage());
          if (Files.exists(filePath)) {
      return Files.readAllBytes(filePath);
    }
//      return Files.readAllBytes(filePath);
      return null;
    }
//    Path filePath = Paths.get(storageDirectory, product.getImage());
//
//    if (Files.exists(filePath)) {
//      return Files.readAllBytes(filePath);
//    }
    return null;
  }

  public String deleteImage(Integer productid) throws IOException {
    Product product = getById(productid);
    if (product.getImage() != null) {
      Path filePath = Paths.get(storageDirectory, product.getImage());
      Files.deleteIfExists(filePath);
      product.setImage(null);
      productRepository.save(product);
      return "Image deleted successfully";
    }
    return "Image not found";
  }

  private String getFileExtension(String fileName) {
    if (fileName == null) {
      return "";
    }
    int dotIndex = fileName.lastIndexOf('.');
    return (dotIndex == -1) ? "" : fileName.substring(dotIndex);
  }

  public boolean existById(Integer id){
    return productRepository.existsByIdAndIsDeletedFalse(id);
  }
}
