package com.app.zware.Controllers;

import com.app.zware.Entities.DisposedGood;
import com.app.zware.Entities.GoodsDisposal;
import com.app.zware.Entities.Item;
import com.app.zware.Entities.User;
import com.app.zware.HttpEntities.CustomResponse;
import com.app.zware.HttpEntities.DisposedGoodsDTO;
import com.app.zware.HttpEntities.GoodsDisposalDTO;
import com.app.zware.Service.GoodsDisposalService;
import com.app.zware.Service.ItemService;
import com.app.zware.Service.UserService;
import com.app.zware.Service.WarehouseItemsService;
import com.app.zware.Service.WarehouseService;
import com.app.zware.Validation.GoodsDisposalValidator;
import com.app.zware.Validation.WarehouseValidator;
import jakarta.servlet.http.HttpServletRequest;
import java.time.LocalDate;
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
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/goods_disposal")
public class GoodsDisposalController {

  @Autowired
  GoodsDisposalService goodsDisposalService;

  @Autowired
  GoodsDisposalValidator goodsDisposalValidator;

  @Autowired
  UserService userService;

  @Autowired
  WarehouseService warehouseService;

  @Autowired
  WarehouseValidator warehouseValidator;

  @Autowired
  WarehouseItemsService warehouseItemsService;

  @Autowired
  ItemService itemService;

  @GetMapping("")
  public ResponseEntity<?> index() {
    //Authorization : ALL

    // response
    CustomResponse customResponse = new CustomResponse();
    customResponse.setAll(true, "Get data of all goods disposal success",
        goodsDisposalService.findAllGoods());
    return new ResponseEntity<>(customResponse, HttpStatus.OK);
  }

  @PostMapping("")
  public ResponseEntity<?> store(@RequestBody GoodsDisposal goods, HttpServletRequest request) {

    //response
    CustomResponse customResponse = new CustomResponse();

    //Authorization : Admin and user quan li kho do
    User user = userService.getRequestMaker(request);
    if (!user.getRole().equals("admin") && !user.getWarehouse_id()
        .equals(goods.getWarehouse_id())) {
      customResponse.setAll(false, "You are not allowed", null);
      return new ResponseEntity<>(customResponse, HttpStatus.UNAUTHORIZED);
    }

    //Validation
    String message = goodsDisposalValidator.checkPost(goods);
    if (!message.isEmpty()) {
      customResponse.setAll(false, message, null);
      return new ResponseEntity<>(message, HttpStatus.BAD_REQUEST);
    }

    GoodsDisposal createdgoods = goodsDisposalService.createGoodsDisposed(goods);
    customResponse.setAll(true, "Goods Disposal created", createdgoods);
    return new ResponseEntity<>(customResponse, HttpStatus.OK);

  }

  @GetMapping("/{id}")
  public ResponseEntity<?> show(@PathVariable("id") Integer id) {
    //Authorization : ALL

    //response
    CustomResponse customResponse = new CustomResponse();

    String message = goodsDisposalValidator.checkGet(id);
    if (!message.isEmpty()) {
      customResponse.setAll(false, message, null);
      return new ResponseEntity<>(customResponse, HttpStatus.BAD_REQUEST);
    }

    customResponse.setAll(true, "get data of goods disposal with id " + id + " success",
        goodsDisposalService.getGoodsById(id));
    return new ResponseEntity<>(customResponse, HttpStatus.OK);

  }

  @DeleteMapping("/{id}")
  public ResponseEntity<?> destroy(@PathVariable("id") Integer id, HttpServletRequest request) {

    //response
    CustomResponse customResponse = new CustomResponse();

    //Authorization : Admin and user quan ly kho do

    User user = userService.getRequestMaker(request);
    GoodsDisposal goods = goodsDisposalService.getGoodsById(id);
    if (!user.getRole().equals("admin") && !user.getWarehouse_id()
        .equals(goods.getWarehouse_id())) {
      customResponse.setAll(false, "You are not allowed", null);
      return new ResponseEntity<>(customResponse, HttpStatus.UNAUTHORIZED);
    }

    String message = goodsDisposalValidator.checkDelete(id);
    if (!message.isEmpty()) {
      customResponse.setAll(false, message, null);
      return new ResponseEntity<>(customResponse, HttpStatus.BAD_REQUEST);
    }

    goodsDisposalService.deleteById(id);
    customResponse.setAll(true, "Goods Disposal with id " + id + " has been deleted", null);
    return new ResponseEntity<>(customResponse, HttpStatus.OK);

  }

  @PutMapping("/{id}")
  public ResponseEntity<?> update(@PathVariable Integer id, @RequestBody GoodsDisposal requestGoods,
      HttpServletRequest request) {

    //response
    CustomResponse customResponse = new CustomResponse();

    //Authorization : Admin and user quan li kho do

    User user = userService.getRequestMaker(request);
    GoodsDisposal goods = goodsDisposalService.getGoodsById(id);
    if (!user.getRole().equals("admin") && !user.getWarehouse_id()
        .equals(goods.getWarehouse_id())) {
      customResponse.setAll(false, "You are not allowed", null);
      return new ResponseEntity<>(customResponse, HttpStatus.UNAUTHORIZED);
    }
    GoodsDisposal mergedGoodsDisposal = goodsDisposalService.merge(id, requestGoods);
    String checkMessage = goodsDisposalValidator.checkPut(id, mergedGoodsDisposal);
    if (!checkMessage.isEmpty()) {
      customResponse.setAll(false, checkMessage, null);
      return new ResponseEntity<>(customResponse, HttpStatus.BAD_REQUEST);
    }

    GoodsDisposal update = goodsDisposalService.update(mergedGoodsDisposal);
    customResponse.setAll(true, "Goods Disposal success", update);
    return new ResponseEntity<>(customResponse, HttpStatus.OK);
  }

  @GetMapping(params = "warehouse_id")
  public ResponseEntity<?> getByWarehouseId(@RequestParam("warehouse_id") Integer warehouseId) {
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
        goodsDisposalService.getByWarehouse(warehouseId));

    return new ResponseEntity<>(customResponse, HttpStatus.OK);
  }

  @PostMapping("/create")
  public ResponseEntity<?> create(
      @RequestBody GoodsDisposalDTO disposalDTO,
      HttpServletRequest request) {
    CustomResponse customResponse = new CustomResponse();

    //authorization
    User requestMaker = userService.getRequestMaker(request);
    if (!requestMaker.getRole().equals("admin") &&
        !requestMaker.getWarehouse_id().equals(disposalDTO.getWarehouse_id())
    ) {
      customResponse.setAll(false, "You are not allowed", null);
      return new ResponseEntity<>(customResponse, HttpStatus.BAD_REQUEST);
    }

    //validation
    String checkMessage = goodsDisposalValidator.checkCreate(disposalDTO);
    if (!checkMessage.isEmpty()) {
      customResponse.setAll(false, checkMessage, null);
      return ResponseEntity.ok(customResponse);
    }

    //validation passed
    GoodsDisposal newDisposal = new GoodsDisposal();
    newDisposal.setWarehouse_id(disposalDTO.getWarehouse_id());
    newDisposal.setMaker_id(requestMaker.getId());
    newDisposal.setDate(LocalDate.now());
    newDisposal.setStatus("completed");

    GoodsDisposal savedDisposal = goodsDisposalService.save(newDisposal);

    for (DisposedGoodsDTO detail : disposalDTO.getDetails()) {
      Item savedItem = itemService.getByProductAndDate(detail.getProduct_id(),
          detail.getExpire_date());
      DisposedGood newDetail = new DisposedGood();
      newDetail.setDisposal_id(savedDisposal.getId());
      newDetail.setItem_id(savedItem.getId());
      newDetail.setQuantity(detail.getQuantity());
      newDetail.setReason(detail.getReason());
      newDetail.setZone_id(detail.getZone_id());

      //Execute remove
      warehouseItemsService.removeFromZone(
          newDetail.getZone_id(), detail.getProduct_id(), detail.getExpire_date(),
          newDetail.getQuantity());
    }

    customResponse.setAll(true, "Create goods disposal success", savedDisposal);
    return ResponseEntity.ok(customResponse);
  }

}
