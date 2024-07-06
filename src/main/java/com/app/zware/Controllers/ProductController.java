package com.app.zware.Controllers;

import com.app.zware.Entities.Product;
import com.app.zware.Entities.User;
import com.app.zware.HttpEntities.CustomResponse;
import com.app.zware.HttpEntities.ProductNonExpiredDTO;
import com.app.zware.Service.ProductService;
import com.app.zware.Service.UserService;
import com.app.zware.Validation.ProductValidator;
import com.app.zware.Validation.WarehouseValidator;
import jakarta.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/products")
public class ProductController {

  @Autowired
  ProductService productService;

  @Autowired
  ProductValidator productValidator;

  @Autowired
  WarehouseValidator warehouseValidator;

  @Autowired
  UserService userService;

  @GetMapping("")
  public ResponseEntity<?> index() {
    //response
    CustomResponse customResponse = new CustomResponse();
    //Authorization: All
    List<Product> productList = productService.getAllProducts();
    if (productList.isEmpty()) {
      customResponse.setAll(false, "List Products are empty", null);
      return new ResponseEntity<>(customResponse, HttpStatus.BAD_REQUEST);
    } else {
      customResponse.setAll(true, "Get data all of Product success", productList);
      return new ResponseEntity<>(customResponse, HttpStatus.OK);
    }
  }

  @PostMapping("")
  public ResponseEntity<?> store(@RequestBody Product product, HttpServletRequest request) {
    //response
    CustomResponse customResponse = new CustomResponse();
    //Authorization: Only Admin can create products
    User user = userService.getRequestMaker(request);
    if (!user.getRole().equals("admin")) {
      customResponse.setAll(false, "You are not allowed", null);
      return new ResponseEntity<>(customResponse, HttpStatus.UNAUTHORIZED);
    }

    //validation
    String messages = productValidator.checkPost(product);
    product.setImage(null);
    if (!messages.isEmpty()) {
      //error
      customResponse.setAll(false, messages, null);
      return new ResponseEntity<>(customResponse, HttpStatus.BAD_REQUEST);
    } else {
      //approve
      customResponse.setAll(true, "Product has been created",
          productService.createProduct(product));
      return new ResponseEntity<>(customResponse, HttpStatus.OK);
    }
  }

  @GetMapping("/{productId}")
  public ResponseEntity<?> show(@PathVariable("productId") Integer productId) {
    //response
    CustomResponse customResponse = new CustomResponse();
    //Authorization: All
    String messages = productValidator.checkGet(productId);
    if (messages.isEmpty()) {
      //approve
      Product product = productService.getById(productId);
      customResponse.setAll(true, "Get data of Product with id:" + productId + " success", product);
      return new ResponseEntity<>(customResponse, HttpStatus.OK);
    } else {
      //error
      customResponse.setAll(false, messages, null);
      return new ResponseEntity<>(customResponse, HttpStatus.BAD_REQUEST);
    }

  }

  @DeleteMapping("/{productId}")
  public ResponseEntity<?> destroy(@PathVariable("productId") Integer productId,
      HttpServletRequest request) {
    //response
    CustomResponse customResponse = new CustomResponse();
    //Authorization : Only Admin
    User user = userService.getRequestMaker(request);
    if (!user.getRole().equals("admin")) {
      customResponse.setAll(false, "You are not allowed", null);
      return new ResponseEntity<>(customResponse, HttpStatus.UNAUTHORIZED);
    }

    //validation
    String msg = productValidator.checkDelete(productId);
    if (!msg.isEmpty()) {
      //error
      customResponse.setAll(false, msg, null);
      return new ResponseEntity<>(customResponse, HttpStatus.BAD_REQUEST);
    } else {
      //approve
      productService.deleteProductById(productId);
      customResponse.setAll(true, "Product with id:" + productId + " has been deleted", null);
      return new ResponseEntity<>(customResponse, HttpStatus.OK);
    }
  }

  @PutMapping("/{productId}")
  public ResponseEntity<?> update(@PathVariable Integer productId, @RequestBody Product request,
      HttpServletRequest userRequest) {
    //response
    CustomResponse customResponse = new CustomResponse();
    //Authorization: Only Admin
    User user = userService.getRequestMaker(userRequest);
    if (!user.getRole().equals("admin")) {
      customResponse.setAll(false, "You are not allowed", null);
      return new ResponseEntity<>(customResponse, HttpStatus.UNAUTHORIZED);
    }

    //set image of new update = null
    request.setImage(null);

    //merge infor
    Product updatedProduct = productService.merger(productId, request);

    //check validate
    String messages = productValidator.checkPut(productId, updatedProduct);

    if (!messages.isEmpty()) {
      //error
      customResponse.setAll(false, messages, null);
      return new ResponseEntity<>(customResponse, HttpStatus.BAD_REQUEST);
    } else {
      //approve
      productService.update(updatedProduct);
      customResponse.setAll(true, "Product has been updated", updatedProduct);
      return new ResponseEntity<>(customResponse, HttpStatus.OK);
    }
  }

  @GetMapping("/non_expire_quantity")
  public ResponseEntity<?> getNonExpireQuantityByWarehouse(@RequestParam("warehouse_id") Integer warehouseId) {
    //response
    CustomResponse customResponse = new CustomResponse();

    //Authorization: all

    //validate
    String checkedMessage = warehouseValidator.checkGet(warehouseId);
    if(!checkedMessage.isEmpty()) {
      customResponse.setAll(false, checkedMessage, null);
      return new ResponseEntity<>(customResponse,HttpStatus.BAD_REQUEST);
    }

    List<ProductNonExpiredDTO> productQuantities = productService.getNonExpireQuantityByWarehouse(warehouseId);

    if (productQuantities.isEmpty()) {
      customResponse.setAll(false, "No products found or all products are expired in the given warehouse", null);
      return new ResponseEntity<>(customResponse, HttpStatus.BAD_REQUEST);
    }
    customResponse.setAll(true, "Non-expired product quantities retrieved successfully", productQuantities);
    return new ResponseEntity<>(customResponse, HttpStatus.OK);
  }

  @PostMapping("/{productid}/image")
  public ResponseEntity<?> putImage(@RequestParam("image") MultipartFile file,
      @PathVariable("productid") Integer productid,
      HttpServletRequest request) throws IOException {
    //response
    CustomResponse customResponse = new CustomResponse();
    //Authorization: Only admin
    User user = userService.getRequestMaker(request);
    if (!user.getRole().equals("admin")) {
      customResponse.setAll(false, "You are not allowed", null);
      return new ResponseEntity<>(customResponse, HttpStatus.UNAUTHORIZED);
    }
    //validation
    String msg = productValidator.checkGet(productid);
    if (!msg.isEmpty()) {
      //error
      customResponse.setAll(false, msg, null);
      return new ResponseEntity<>(customResponse, HttpStatus.BAD_REQUEST);
    } else if (file.isEmpty()) {
      //File image not found
      customResponse.setAll(false, "Not found file!", null);
      return new ResponseEntity<>(customResponse, HttpStatus.BAD_REQUEST);
    } else {
      //upload image success
      String uploadImage = productService.uploadImage(file, productid);
      customResponse.setAll(true, productService.uploadImage(file, productid), null);
      return new ResponseEntity<>(customResponse, HttpStatus.OK);
    }
  }

  @GetMapping("/{productid}/image")
  public ResponseEntity<?> showImage(@PathVariable Integer productid)
      throws IOException {
    //response
    CustomResponse customResponse = new CustomResponse();
    //Authorization: All

    //validation
    String msg = productValidator.checkGet(productid);
    if (!msg.isEmpty()) {
      //error
      customResponse.setAll(false, msg, null);
      return new ResponseEntity<>(customResponse, HttpStatus.BAD_REQUEST);
    } else {
      byte[] imageData = productService.downloadImage(productid);
      if (imageData != null) {
        return ResponseEntity.status(HttpStatus.OK)
            .contentType(MediaType.IMAGE_JPEG)
            .body(imageData);
      } else {
        //not found image
        customResponse.setAll(false, "Image not found", null);
        return new ResponseEntity<>(customResponse, HttpStatus.BAD_REQUEST);
      }
    }
  }

  @DeleteMapping("/{productid}/image")
  public ResponseEntity<?> destroyImage(@PathVariable Integer productid, HttpServletRequest request)
      throws IOException {
    //response
    CustomResponse customResponse = new CustomResponse();
    //Authorization: Only admin
    User user = userService.getRequestMaker(request);
    if (!user.getRole().equals("admin")) {
      customResponse.setAll(false, "You are not allowed", null);
      return new ResponseEntity<>(customResponse, HttpStatus.UNAUTHORIZED);
    }
    //validation
    String msg = productValidator.checkDelete(productid);
    if (!msg.isEmpty()) {
      //error
      customResponse.setAll(false, msg, null);
      return new ResponseEntity<>(customResponse, HttpStatus.BAD_REQUEST);
    } else {
      String result = productService.deleteImage(productid);
      if (result.equals("Image deleted successfully")) {
        //delete success
        customResponse.setAll(true, result, null);
        return new ResponseEntity<>(customResponse, HttpStatus.OK);
      } else {
        //error delete
        customResponse.setAll(false, result, null);
        return new ResponseEntity<>(customResponse, HttpStatus.BAD_REQUEST);
      }
    }
  }

//  @Bean
//  public MultipartConfigElement multipartConfigElement() {
//    MultipartConfigFactory factory = new MultipartConfigFactory();
//    // Tăng giới hạn kích thước tệp lên 50MB
//    factory.setMaxFileSize(DataSize.parse("50MB"));
//    // Tăng giới hạn kích thước tổng cả các tệp lên 100MB
//    factory.setMaxRequestSize(DataSize.parse("100MB"));
//    return factory.createMultipartConfig();
//  }
}
