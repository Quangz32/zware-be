package com.app.zware.Controllers;


import com.app.zware.Entities.User;
import com.app.zware.Entities.Warehouse;
import com.app.zware.HttpEntities.CustomResponse;
import com.app.zware.Service.UserService;
import com.app.zware.Service.WarehouseService;
import com.app.zware.Validation.WarehouseValidator;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/warehouses")
public class WarehouseController {

  @Autowired
  WarehouseService warehouseService;

  @Autowired
  WarehouseValidator warehouseValidator;

  @Autowired
  UserService userService;

  @GetMapping("")
  public ResponseEntity<?> index() {
    //Authorization : ALL

    //response
    CustomResponse customResponse = new CustomResponse();

    //Finally
    customResponse.setAll(true, "Get data of all warehouses success",
        warehouseService.getWarehouse());

    return new ResponseEntity<>(customResponse, HttpStatus.OK);
  }

  @PostMapping("")
  public ResponseEntity<?> store(@RequestBody Warehouse wareHouseRequest,
      HttpServletRequest request) {
    // response
    CustomResponse customResponse = new CustomResponse();

    // Authorization : Admin
    User user = userService.getRequestMaker(request);
    if (!user.getRole().equals("admin")) {
      customResponse.setAll(false, "You are not allowed", null);
      return new ResponseEntity<>(customResponse, HttpStatus.UNAUTHORIZED);
    }

    //Validation
    String message = warehouseValidator.checkPost(wareHouseRequest);
    if (!message.isEmpty()) {
      customResponse.setAll(false, message, null);
      return new ResponseEntity<>(customResponse, HttpStatus.BAD_REQUEST);
    }

    //finally
    Warehouse createdWarehouse = warehouseService.createWareHouse(wareHouseRequest);
    customResponse.setAll(true, "Warehouse created", createdWarehouse);
    return new ResponseEntity<>(customResponse, HttpStatus.OK);
  }

  @GetMapping("/{warehouseId}")
  public ResponseEntity<?> show(@PathVariable("warehouseId") Integer warehouseId) {
    //Authorization : ALL

    //Response
    CustomResponse customResponse = new CustomResponse();

    // Validation
    String checkMessage = warehouseValidator.checkGet(warehouseId);
    if (!checkMessage.isEmpty()) {
      customResponse.setAll(false, checkMessage, null);
      return new ResponseEntity<>(customResponse, HttpStatus.BAD_REQUEST);
    }

    //finally
    customResponse.setAll(
        true,
        "Get warehouse data success",
        warehouseService.getWareHouseById(warehouseId)
    );

    return new ResponseEntity<>(customResponse, HttpStatus.OK);

  }

  @GetMapping("/{warehouseId}/zones")
  public ResponseEntity<?> getZones(
      @PathVariable("warehouseId") Integer warehouseId
  ) {
    //Response
    CustomResponse customResponse = new CustomResponse();

    //Authorization: All

    //Validation
    String checkMessage = warehouseValidator.checkGet(warehouseId);
    if (!checkMessage.isEmpty()) {
      customResponse.setAll(false, checkMessage, null);
      return new ResponseEntity<>(customResponse, HttpStatus.BAD_REQUEST);
    }

    //finally
    customResponse.setAll(
        true,
        "Get zones by warehouse success",
        warehouseService.getZonesByWarehouseId(warehouseId));

    return new ResponseEntity<>(customResponse, HttpStatus.OK);
  }


  @GetMapping("/{warehouseId}/items")
  public ResponseEntity<?> getItem(@PathVariable("warehouseId") Integer warehouseId) {
    //response
    CustomResponse customResponse = new CustomResponse();

    //Authorization : ALL

    //Validation
    String checkMessage = warehouseValidator.checkGet(warehouseId);
    if (!checkMessage.isEmpty()) {
      customResponse.setAll(false, checkMessage, null);
      return new ResponseEntity<>(customResponse, HttpStatus.OK);

    }

    //finally
    customResponse.setAll(true, "Get Items by warehouse success",
        warehouseService.getItemsByWarehouseId(warehouseId));

    return new ResponseEntity<>(customResponse, HttpStatus.OK);

  }

  @DeleteMapping("/{warehouseId}")
  public ResponseEntity<?> destroy(@PathVariable("warehouseId") Integer warehouseId,
      HttpServletRequest request) {
    //Response
    CustomResponse customResponse = new CustomResponse();

    //Authorization : Admin
    User user = userService.getRequestMaker(request);
    if (!user.getRole().equals("admin")) {
      customResponse.setAll(false, "You are not allowed", null);
      return new ResponseEntity<>(customResponse, HttpStatus.UNAUTHORIZED);
    }

    //Validation
    String checkMessage = warehouseValidator.checkDetete(warehouseId);
    if (!checkMessage.isEmpty()) {
      customResponse.setAll(false, checkMessage, null);
      return new ResponseEntity<>(customResponse, HttpStatus.BAD_REQUEST);
    }

    //Finally
    warehouseService.deleteWareHouseById(warehouseId);
    customResponse.setAll(true, "Delete warehouse success", null);
    return new ResponseEntity<>(customResponse, HttpStatus.OK);

  }

  @PutMapping("/{warehouseId}")
  public ResponseEntity<?> update(@PathVariable int warehouseId,
      @RequestBody Warehouse requestWarehouse, HttpServletRequest request) {

    //Response
    CustomResponse customResponse = new CustomResponse();

    //Authorization : Admin
    User user = userService.getRequestMaker(request);
    if (!user.getRole().equals("admin")) {
      customResponse.setAll(false, "You are not allowed", null);
      return new ResponseEntity<>(customResponse, HttpStatus.UNAUTHORIZED);
    }

    Warehouse mergedWarehouse = warehouseService.merge(warehouseId, requestWarehouse);

    String checkMessage = warehouseValidator.checkPut(warehouseId, mergedWarehouse);

    if (!checkMessage.isEmpty()) {
      customResponse.setAll(false, checkMessage, null);
      return new ResponseEntity<>(customResponse, HttpStatus.BAD_REQUEST);
    }

    Warehouse updateWarehouse = warehouseService.updateWarehouse(mergedWarehouse);
    customResponse.setAll(true, "Warehouse update success", updateWarehouse);
    return new ResponseEntity<>(customResponse, HttpStatus.OK);

  }


  @GetMapping("/{warehouseId}/inbound_transactions")
  public ResponseEntity<?> getInbound(@PathVariable("warehouseId") Integer warehouseId) {
    //response
    CustomResponse customResponse = new CustomResponse();

    //Authorization : ALL

    //Validation
    String checkMessage = warehouseValidator.checkGet(warehouseId);
    if (!checkMessage.isEmpty()) {
      customResponse.setAll(false, checkMessage, null);
      return new ResponseEntity<>(customResponse, HttpStatus.BAD_REQUEST);

    }

    //finally
    customResponse.setAll(true, "Get Inbound Transaction by warehouse success",
            warehouseService.getInboundByWarehouseId(warehouseId));

    return new ResponseEntity<>(customResponse, HttpStatus.OK);

  }

  @GetMapping("/{warehouseId}/goods_disposal")
  public ResponseEntity<?> getGoodsDisposal(@PathVariable("warehouseId") Integer warehouseId){
    //response
    CustomResponse customResponse = new CustomResponse();

    //Authorization : ALL

    //Validation
    String checkMessage = warehouseValidator.checkGet(warehouseId);
    if (!checkMessage.isEmpty()) {
      customResponse.setAll(false, checkMessage, null);
      return new ResponseEntity<>(customResponse, HttpStatus.BAD_REQUEST);

    }

    //finally
    customResponse.setAll(true, "Get Goods Disposal by warehouse success",
            warehouseService.getGoodsDisposalByWarehouseId(warehouseId));

    return new ResponseEntity<>(customResponse, HttpStatus.OK);
  }

  @GetMapping("/{warehouseId}/outbound_transactions")
  public ResponseEntity<?> getOutbound(@PathVariable("warehouseId") Integer warehouseId){
    //response
    CustomResponse customResponse = new CustomResponse();

    //Authorization : ALL

    //Validation
    String checkMessage = warehouseValidator.checkGetOutbound(warehouseId);
    if (!checkMessage.isEmpty()) {
      customResponse.setAll(false, checkMessage, null);
      return new ResponseEntity<>(customResponse, HttpStatus.BAD_REQUEST);
    }

    //finally
    customResponse.setAll(true, "Get Outbound Transaction by warehouse success",
            warehouseService.getOutboundByWarehouseId(warehouseId));

    return new ResponseEntity<>(customResponse, HttpStatus.OK);
  }
}


