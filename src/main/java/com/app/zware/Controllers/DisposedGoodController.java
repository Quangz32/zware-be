package com.app.zware.Controllers;

import com.app.zware.Entities.*;
import com.app.zware.HttpEntities.CustomResponse;
import com.app.zware.Repositories.DisposedGoodRespository;
import com.app.zware.Repositories.GoodsDisposalRepository;
import com.app.zware.Repositories.WarehouseItemsRepository;
import com.app.zware.Service.*;
import com.app.zware.Validation.DisposedGoodValidator;
import com.app.zware.Validation.GoodsDisposalValidator;
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
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/disposed_goods")
public class DisposedGoodController {

  @Autowired
  DisposedGoodService disposedGoodService;

  @Autowired
  DisposedGoodValidator disposedGoodValidator;

  @Autowired
  UserService userService;

  @Autowired
  GoodsDisposalService goodsDisposalService;

  @Autowired
  GoodsDisposalValidator goodsDisposalValidator;

  @Autowired
  WarehouseItemsRepository warehouseItemsRepository;

  @Autowired
  GoodsDisposalRepository goodsDisposalRepository;

  @Autowired
  ItemService itemService;

  @Autowired
  DisposedGoodRespository disposedGoodRespository;

  @Autowired
  WarehouseItemsService warehouseItemsService;

  @Autowired
  WarehouseValidator warehouseValidator;

  @GetMapping("")
  public ResponseEntity<?> index() {
    //Authorization : ALL

    //response
    CustomResponse customResponse = new CustomResponse();
    customResponse.setAll(true, "Get data all of disposed good success",
        disposedGoodService.getAllDisposedGood());
    return new ResponseEntity<>(customResponse, HttpStatus.OK);
  }

  @PostMapping("")
  public ResponseEntity<?> store(@RequestBody DisposedGood newDisposedGood,
      HttpServletRequest request) {
    //response
    CustomResponse customResponse = new CustomResponse();

    //Authorization : Admin and manager warehouse
    User userRequestMaker = userService.getRequestMaker(request);

    GoodsDisposal goodsDisposal = goodsDisposalService.getGoodsById(
        newDisposedGood.getDisposal_id());
    //Authorization
    if (!userRequestMaker.getRole().equals("admin") && !userRequestMaker.getWarehouse_id()
        .equals(goodsDisposal.getWarehouse_id())) {
      customResponse.setAll(false, "You are not allowed", null);
      return new ResponseEntity<>(customResponse, HttpStatus.UNAUTHORIZED);
    }

    // validation
    String checkMessage = disposedGoodValidator.checkPost(newDisposedGood);
    if (!checkMessage.isEmpty()) {
      customResponse.setAll(false, checkMessage, null);
      return new ResponseEntity<>(customResponse, HttpStatus.BAD_REQUEST);
    }

    //finally

    DisposedGood createdDisposedGood = disposedGoodService.createDisposedGood(newDisposedGood);
    customResponse.setAll(true, "Disposed good created", createdDisposedGood);
    return new ResponseEntity<>(customResponse, HttpStatus.OK);
  }


  @GetMapping("/{id}")
  public ResponseEntity<?> show(@PathVariable("id") Integer id) {
    //Authorization : ALL

    //response
    CustomResponse customResponse = new CustomResponse();

    String checkMessage = disposedGoodValidator.checkGet(id);
    if (!checkMessage.isEmpty()) {
      customResponse.setAll(false, checkMessage, null);
      return new ResponseEntity<>(customResponse, HttpStatus.BAD_REQUEST);
    }

    customResponse.setAll(true, "get data of disposed good with id " + id + " success",
        disposedGoodService.getDisposedGoodById(id));
    return new ResponseEntity<>(customResponse, HttpStatus.OK);
  }


  @DeleteMapping("/{id}")
  public ResponseEntity<?> destroy(@PathVariable("id") Integer id, HttpServletRequest request) {
    //Response
    CustomResponse customResponse = new CustomResponse();

    //Authorization : Admin and manager

    DisposedGood disposedGood = disposedGoodService.getDisposedGoodById(id);
    if (disposedGood == null) {
      customResponse.setAll(false, "Disposed Good is null", null);
      return new ResponseEntity<>(customResponse, HttpStatus.NOT_FOUND);
    }
    GoodsDisposal goodsDisposal = goodsDisposalService.getGoodsById(disposedGood.getDisposal_id());

    User userRequestMaker = userService.getRequestMaker(request);
    if (!userRequestMaker.getRole().equals("admin") && !userRequestMaker.getWarehouse_id()
        .equals(goodsDisposal.getWarehouse_id())) {
      customResponse.setAll(false, "You are not allowed", null);
      return new ResponseEntity<>(customResponse, HttpStatus.UNAUTHORIZED);
    }

    String checkMessgae = disposedGoodValidator.checkDelete(id);
    if (!checkMessgae.isEmpty()) {
      customResponse.setAll(false, checkMessgae, null);
      return new ResponseEntity<>(customResponse, HttpStatus.BAD_REQUEST);
    }

    //finally
    disposedGoodService.deleteDisposedGood(id);
    customResponse.setAll(true, "Disposed Good with id " + id + " has been deleted", null);
    return new ResponseEntity<>(customResponse, HttpStatus.OK);
  }


  @PutMapping("/{id}")
  public ResponseEntity<?> update(@PathVariable Integer id,
      @RequestBody DisposedGood requestDisposedGood, HttpServletRequest request) {
    //Authorization : Admin and manager
    // response
    CustomResponse customResponse = new CustomResponse();

    DisposedGood disposedGood = disposedGoodService.getDisposedGoodById(id);
    if (disposedGood == null) {
      customResponse.setAll(false, "Disposed Good is null", null);
      return new ResponseEntity<>(customResponse, HttpStatus.NOT_FOUND);
    }
    GoodsDisposal goodsDisposal = goodsDisposalService.getGoodsById(disposedGood.getDisposal_id());

    User userRequestMaker = userService.getRequestMaker(request);
    if (!userRequestMaker.getRole().equals("admin") && !userRequestMaker.getWarehouse_id()
        .equals(goodsDisposal.getWarehouse_id())) {
      customResponse.setAll(false, "You are not allowed", null);
      return new ResponseEntity<>(customResponse, HttpStatus.UNAUTHORIZED);
    }

    DisposedGood mergeDisposedGood = disposedGoodService.merge(id, requestDisposedGood);

    String checkMessage = disposedGoodValidator.checkPut(id, mergeDisposedGood);
    if (!checkMessage.isEmpty()) {
      customResponse.setAll(false, checkMessage, null);
      return new ResponseEntity<>(customResponse, HttpStatus.BAD_REQUEST);
    }

    //update
    DisposedGood updated = disposedGoodService.update(mergeDisposedGood);
    customResponse.setAll(true, "Disposed update success", updated);
    return new ResponseEntity<>(customResponse, HttpStatus.OK);

  }

  @GetMapping(params = "disposal_id")
  public ResponseEntity<?> getByDisposal(
      @RequestParam("disposal_id") Integer goodDisposalId) {
    //Response
    CustomResponse customResponse = new CustomResponse();

    //Authorization : ALL

    //Validation
    String checkMessage = goodsDisposalValidator.checkGet(goodDisposalId);
    if (!checkMessage.isEmpty()) {
      customResponse.setAll(false, checkMessage, null);
      return new ResponseEntity<>(customResponse, HttpStatus.BAD_REQUEST);

    }

    //finally
    customResponse.setAll(false, "get disposal good by goods disposal success ",
        disposedGoodService.getByGoodDisposal(goodDisposalId));
    return new ResponseEntity<>(customResponse, HttpStatus.OK);
  }

  // xoa tat ca ca san pham het han trong 1 kho
  @PostMapping("/remove_expire_product_by_warehouse/{warehouseId}")
  public ResponseEntity<?> removeExpiredProductByWarehouse (@PathVariable("warehouseId") Integer warehouseId,HttpServletRequest request) {
    // Response
    CustomResponse customResponse = new CustomResponse();

    // Authorization ( de sau)
    User requestMaker = userService.getRequestMaker(request);
    boolean isAdmin = "admin".equals(requestMaker.getRole());

    boolean isManager = !isAdmin && requestMaker.getWarehouse_id() != null &&
            requestMaker.getWarehouse_id().equals(warehouseId);

    // Authorization
    if (!isAdmin && !isManager){
      customResponse.setAll(false,"You are not allowed",null);
      return new ResponseEntity<>(customResponse,HttpStatus.UNAUTHORIZED);
    }

    // Validation
    String message = warehouseValidator.checkGet(warehouseId);
    if(!message.isEmpty()){
      customResponse.setAll(false,message,null);
      return new ResponseEntity<>(customResponse,HttpStatus.NOT_FOUND);
    }

    List<WarehouseItems> expriredItems = warehouseItemsRepository.findExpiredByWarehouse(warehouseId);
    if (expriredItems == null||expriredItems.isEmpty()) {
      customResponse.setAll(false, "No expired products found in warehouse" , null);
      return new ResponseEntity<>(customResponse, HttpStatus.NOT_FOUND);
    }

    // Tao phieu huy hang
    GoodsDisposal disposal = new GoodsDisposal();
    disposal.setWarehouse_id(warehouseId);
    disposal.setMaker_id(requestMaker.getId());
    disposal.setDate(LocalDate.now());
    disposal.setStatus("complete");

    // Save goodDisposal
    GoodsDisposal savedDisposal = goodsDisposalRepository.save(disposal);

    for (WarehouseItems warehouseItems : expriredItems) {
      DisposedGood disposedGood = new DisposedGood();
      disposedGood.setDisposal_id(savedDisposal.getId());
      disposedGood.setReason("expired");
      Item itemExpired = itemService.getItemById(warehouseItems.getItem_id());
      disposedGood.setItem_id(itemExpired.getId());
      disposedGood.setQuantity(warehouseItems.getQuantity());
      disposedGood.setZone_id(warehouseItems.getZone_id());

      disposedGoodRespository.save(disposedGood);

      // remove
      warehouseItemsService.removeFromZone(warehouseItems.getZone_id(), itemExpired.getProduct_id(), itemExpired.getExpire_date(), warehouseItems.getQuantity());
    }
    customResponse.setAll(true, "Delete Expired Items by Warehouse success", null);
    return new ResponseEntity<>(customResponse, HttpStatus.OK);
  }
}
