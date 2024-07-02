package com.app.zware.Controllers;


import com.app.zware.Entities.User;
import com.app.zware.Entities.WarehouseItems;
import com.app.zware.Entities.WarehouseZone;
import com.app.zware.HttpEntities.CustomResponse;
import com.app.zware.Repositories.WarehouseZoneRespository;
import com.app.zware.Service.UserService;
import com.app.zware.Service.WarehouseItemsService;
import com.app.zware.Service.WarehouseZoneService;
import com.app.zware.Validation.ProductValidator;
import com.app.zware.Validation.WarehouseItemValidator;
import com.app.zware.Validation.WarehouseValidator;
import com.app.zware.Validation.WarehouseZoneValidator;
import jakarta.servlet.http.HttpServletRequest;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/warehouse_items")
public class WarehouseItemsController {

  @Autowired
  WarehouseItemsService warehouseItemsService;

  @Autowired
  WarehouseItemValidator warehouseItemValidator;

  @Autowired
  WarehouseZoneRespository warehouseZoneRespository;

  @Autowired
  UserService userService;

  @Autowired
  WarehouseZoneService warehouseZoneService;

  @Autowired
  WarehouseZoneValidator warehouseZoneValidator;

  @Autowired
  WarehouseValidator warehouseValidator;

  @Autowired
  ProductValidator productValidator;

  @GetMapping("")
  public ResponseEntity<?> index() {
    //response
    CustomResponse customResponse = new CustomResponse();
    //Authorization : ALL

    List<WarehouseItems> warehouseItemsList = warehouseItemsService.getAllWarehouseItems();
    if (warehouseItemsList.isEmpty()) {
      //empty list
      customResponse.setAll(false, "List WarehouseItems are empty", null);
      return new ResponseEntity<>(customResponse, HttpStatus.BAD_REQUEST);
    } else {
      //success
      customResponse.setAll(true, "Get data of all WarehouseItem success !", warehouseItemsList);
      return new ResponseEntity<>(customResponse, HttpStatus.OK);
    }
  }


  @PostMapping("")
  public ResponseEntity<?> store(@RequestBody WarehouseItems requestWarehouseItem,
      HttpServletRequest request) {
    //response
    CustomResponse customResponse = new CustomResponse();

    //Authorization : Admin and manager

    User user = userService.getRequestMaker(request);

    WarehouseZone warehouseZone = warehouseZoneService.getWarehouseZoneById(
        requestWarehouseItem.getZone_id());

    if (!user.getRole().equals("admin") && !user.getWarehouse_id()
        .equals(warehouseZone.getWarehouse_id())) {
      customResponse.setAll(false, "You are not allowed !", null);
      return new ResponseEntity<>(customResponse, HttpStatus.UNAUTHORIZED);
    }

    String checkMessage = warehouseItemValidator.checkPost(requestWarehouseItem);
    if (!checkMessage.isEmpty()) {
      //error
      customResponse.setAll(false, checkMessage, null);
      return new ResponseEntity<>(customResponse, HttpStatus.BAD_REQUEST);
    } else {
      //approve
      warehouseItemsService.createWarehouseItems(requestWarehouseItem);
      customResponse.setAll(true, "WarehouseItems has been created successfully",
          requestWarehouseItem);
      return new ResponseEntity<>(customResponse, HttpStatus.OK);

    }

  }


  @GetMapping("/{warehouseitemid}")
  public ResponseEntity<?> show(@PathVariable("warehouseitemid") Integer warehouseitemId) {
    //response
    CustomResponse customResponse = new CustomResponse();
    //Authorization : ALL

    String checkMessage = warehouseItemValidator.checkGet(warehouseitemId);
    if (!checkMessage.isEmpty()) {
      //error
      customResponse.setAll(false, checkMessage, null);
      return new ResponseEntity<>(customResponse, HttpStatus.BAD_REQUEST);
    } else {
      //approve
      customResponse.setAll(true, "Get data of warehouseItem with id: " + warehouseitemId +
          " has been success", warehouseItemsService.getById(warehouseitemId));
      return new ResponseEntity<>(customResponse, HttpStatus.OK);
    }
  }


  @DeleteMapping("/{warehouseitemid}")
  public ResponseEntity<?> destroy(@PathVariable("warehouseitemid") Integer warehouseitemId,
      HttpServletRequest request) {
    //response
    CustomResponse customResponse = new CustomResponse();
    // Authorization : Admin and manager

    User user = userService.getRequestMaker(request);
    WarehouseItems warehouseItems = warehouseItemsService.getById(warehouseitemId);
    if (warehouseItems == null) {
      customResponse.setAll(false, "WarehouseItem not found to delete !", null);
      return new ResponseEntity<>(customResponse, HttpStatus.BAD_REQUEST);
    }
    WarehouseZone warehouseZone = warehouseZoneService.getWarehouseZoneById(
        warehouseItems.getZone_id());

    //Authorization
    if (!user.getRole().equals("admin") && (!user.getWarehouse_id()
        .equals(warehouseZone.getWarehouse_id()))) {
      customResponse.setAll(false, "You are not allowed !", null);
      return new ResponseEntity<>(customResponse, HttpStatus.UNAUTHORIZED);
    }

    String checkMessage = warehouseItemValidator.checkDelete(warehouseitemId);
    if (!checkMessage.isEmpty()) {
      //error
      customResponse.setAll(false, checkMessage, null);
      return new ResponseEntity<>(customResponse, HttpStatus.BAD_REQUEST);
    } else {
      warehouseItemsService.deleteWarehouseItemsById(warehouseitemId);
      customResponse.setAll(true,
          "WarehouseItem with id: " + warehouseitemId + " has been deleted successfully", null);
      return new ResponseEntity<>(customResponse, HttpStatus.OK);
    }
  }

  @PutMapping("/{warehouseitemid}")
  public ResponseEntity<?> update(@PathVariable Integer warehouseitemid,
      @RequestBody WarehouseItems requestWarehouseItem, HttpServletRequest request) {
    //response
    CustomResponse customResponse = new CustomResponse();
    //Authorization
    User user = userService.getRequestMaker(request);

    WarehouseItems warehouseItems = warehouseItemsService.getById(warehouseitemid);
    if (warehouseItems == null) {
      customResponse.setAll(false, "WarehouseItem not found to updated !", null);
      return new ResponseEntity<>(customResponse, HttpStatus.BAD_REQUEST);
    }
    WarehouseZone warehouseZone = warehouseZoneService.getWarehouseZoneById(
        warehouseItems.getZone_id());
    if (!user.getRole().equals("admin") && !user.getWarehouse_id()
        .equals(warehouseZone.getWarehouse_id())) {
      customResponse.setAll(false, "You are not allowed !", null);
      return new ResponseEntity<>(customResponse, HttpStatus.UNAUTHORIZED);
    }

    WarehouseItems mergedWarehouseItem = warehouseItemsService.merge(warehouseitemid,
        requestWarehouseItem);

    //Validation
    String checkMessage = warehouseItemValidator.checkPut(warehouseitemid, mergedWarehouseItem);
    if (!checkMessage.isEmpty()) {
      //error
      customResponse.setAll(false, checkMessage, null);
      return new ResponseEntity<>(customResponse, HttpStatus.BAD_REQUEST);
    } else {
      WarehouseItems updated = warehouseItemsService.update(mergedWarehouseItem);
      customResponse.setAll(true,
          "WarehouseItem with id : " + warehouseitemid + " has been updated", updated);
      return new ResponseEntity<>(customResponse, HttpStatus.OK);
    }


  }
  @GetMapping(params = "zone_id")
  public ResponseEntity<?> getByZone(
          @RequestParam("zone_id") Integer zoneId) {

    //Response
    CustomResponse customResponse = new CustomResponse();

    //Authorization : ALL

    //Validation
    String checkMessage = warehouseZoneValidator.checkGet(zoneId);
    if (!checkMessage.isEmpty()) {
      customResponse.setAll(false, checkMessage, null);
      return new ResponseEntity<>(customResponse, HttpStatus.OK);
    }

    //finally
    customResponse.setAll(true, "Get Warehouse Item By Zone success",warehouseItemsService.findByZoneId(zoneId));

    return new ResponseEntity<>(customResponse, HttpStatus.OK);
  }


  @GetMapping(params = "warehouse_id")
  public ResponseEntity<?> getByWarehouse(
          @RequestParam("warehouse_id") Integer warehouseId) {

    //Response
    CustomResponse customResponse = new CustomResponse();

    //Authorization : ALL

    //Validation
    String checkMessage = warehouseValidator.checkGet(warehouseId);
    if (!checkMessage.isEmpty()) {
      customResponse.setAll(false, checkMessage, null);
      return new ResponseEntity<>(customResponse, HttpStatus.OK);
    }

    //finally
    customResponse.setAll(true, "Get Warehouse Item By Warehouse success",warehouseItemsService.findByWarehouseId(warehouseId));

    return new ResponseEntity<>(customResponse, HttpStatus.OK);
  }

  //
  @GetMapping(params = {"product_id", "warehouse_id"})
  public ResponseEntity<?> getByProductAndWarehouse(
          @RequestParam("product_id") Integer productId,
          @RequestParam("warehouse_id") Integer warehouseId) {

    //Response
    CustomResponse customResponse = new CustomResponse();

    //Authorization : ALL

    //Validation

    String checkWarehouseId = warehouseValidator.checkGet(warehouseId);
    String checkProductId = productValidator.checkGet(productId);
     if (!checkWarehouseId.isEmpty()) {
      customResponse.setAll(false, checkWarehouseId, null);
      return new ResponseEntity<>(customResponse, HttpStatus.OK);
    }



    //finally
    customResponse.setAll(true, "Get Warehouse Item By Warehouse and Product success",warehouseItemsService.findByProductAndWarehouse(productId,warehouseId));

    return new ResponseEntity<>(customResponse, HttpStatus.OK);
  }

  @GetMapping(params = "product_id")
  public ResponseEntity<?> getByProduct(
          @RequestParam("product_id") Integer productId) {

    //Response
    CustomResponse customResponse = new CustomResponse();

    //Authorization : ALL

    //Validation
    String checkMessage = productValidator.checkGet(productId);
    if (!checkMessage.isEmpty()) {
      customResponse.setAll(false, checkMessage, null);
      return new ResponseEntity<>(customResponse, HttpStatus.OK);
    }

    //finally
    customResponse.setAll(true, "Get Warehouse Item By Product success",warehouseItemsService.findByProductId(productId));

    return new ResponseEntity<>(customResponse, HttpStatus.OK);
  }




}
