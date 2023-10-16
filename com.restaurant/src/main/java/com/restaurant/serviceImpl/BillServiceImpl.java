package com.restaurant.serviceImpl;

import com.google.common.base.Strings;
import com.google.gson.JsonArray;
import com.google.gson.annotations.JsonAdapter;
import com.itextpdf.text.*;
import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfWriter;
import com.restaurant.JWT.JwtFilter;
import com.restaurant.POJO.Bill;
import com.restaurant.constants.RestaurantConstants;
import com.restaurant.dao.BillDao;
import com.restaurant.service.BillService;
import com.restaurant.utils.RestaurantUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.pdfbox.io.IOUtils;
import org.json.JSONArray;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.lang.annotation.Documented;
import java.util.*;
import java.util.List;
import java.util.stream.Stream;

@Slf4j
@Service
public class BillServiceImpl implements BillService {

    @Autowired
    BillDao billDao;

    @Autowired
    JwtFilter jwtFilter;

    @Override
    public ResponseEntity<String> generateReport(Map<String, Object> requestMap) {
        log.info("Inside generateReport");
        try {
            String fileName;
            if (validateRequestMap(requestMap)) {
                if (requestMap.containsKey("isGenerate") && !(Boolean) requestMap.get("isGenerate")) {
                    fileName = (String) requestMap.get("uuid");
                } else {
                    fileName = RestaurantUtils.getUUID();
                    requestMap.put("uuid", fileName);
                    insertBill(requestMap);
                }
                Document document = new Document();
                PdfWriter.getInstance(document, new FileOutputStream(RestaurantConstants.SAVE_LOCATION + "\\" + fileName + ".pdf"));
                document.open();
                setRectangleInPdf(document);
                Paragraph header = new Paragraph("Food Hub Receipt", getFont("Header"));
                header.setAlignment(Element.ALIGN_CENTER);
                document.add(header);

                String data = "Name: " + requestMap.get("name") + "\n" + "Contact Number: " + requestMap.get("contactNumber") + "\n"
                        + "Email: " + requestMap.get("email") + "\n" + "Payment Method: " + requestMap.get("paymentMethod");
                Paragraph paragraph = new Paragraph(data + "\n \n", getFont("Data"));
                document.add(paragraph);

                PdfPTable table = new PdfPTable(5);
                table.setWidthPercentage(100);
                addTableHeader(table);

                JSONArray jsonArray = new JSONArray((String) requestMap.get("productDetails"));

                for (int i = 0; i < jsonArray.length(); i++) {
                    addRows(table, RestaurantUtils.getMapFromJson(jsonArray.getString(i)));
                }

                document.add(table);

                Paragraph footer = new Paragraph("Total : $" + requestMap.get("totalAmount") + "\n" +
                        "Thank you for visiting. Please visit again!", getFont("Data"));
                document.add(footer);
                document.close();

                return new ResponseEntity<>("{\"uuid\":\"" + fileName + "\"}", HttpStatus.OK);

            }
            return RestaurantUtils.getResponseEntity("Required data not found.", HttpStatus.BAD_REQUEST);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return RestaurantUtils.getResponseEntity(RestaurantConstants.SOMETHING_WENT_WRONG, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @Override
    public ResponseEntity<List<Bill>> getBills() {
        try {
            List<Bill> bills;
            if (jwtFilter.isAdmin()) {
                bills = billDao.getAllBills();
            } else {
                bills = billDao.getBillsByUsername(jwtFilter.getCurrentUser());
            }
            return new ResponseEntity<>(bills, HttpStatus.OK);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return new ResponseEntity<>(Collections.emptyList(), HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @Override
    public ResponseEntity<byte[]> getPdf(Map<String, Object> requestMap) {
        log.info("Inside getPdf : requestMap {}", requestMap);
        try {
            byte[] pdfByteArray = new byte[0];
            if (!requestMap.containsKey("uuid") && validateRequestMap(requestMap)) {
                return new ResponseEntity<>(pdfByteArray, HttpStatus.BAD_REQUEST);
            }
            String filePath = RestaurantConstants.SAVE_LOCATION + "\\" + (String) requestMap.get("uuid") + ".pdf";
            if (!RestaurantUtils.doesFileExist(filePath)) {
                requestMap.put("isGenerate", false);
                generateReport(requestMap);
            }
            pdfByteArray = getPdfByteArray(filePath);
            return new ResponseEntity<>(pdfByteArray, HttpStatus.OK);
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        return null;
    }

    @Override
    public ResponseEntity<String> deleteBill(Integer id) {
        log.info("Inside deleteBill");
        try {
            Optional bill = billDao.findById(id);
            if (bill.isPresent()) {
                billDao.deleteById(id);
                return RestaurantUtils.getResponseEntity("Bill deleted successfully.", HttpStatus.OK);
            }
            return RestaurantUtils.getResponseEntity("Bill id does not exist.", HttpStatus.OK);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return RestaurantUtils.getResponseEntity(RestaurantConstants.SOMETHING_WENT_WRONG, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    private byte[] getPdfByteArray(String filePath) throws Exception {
        File file = new File(filePath);
        InputStream targetStream = new FileInputStream(file);
        byte[] pdfByteArray = IOUtils.toByteArray(targetStream);
        targetStream.close();
        return pdfByteArray;
    }

    private void addRows(PdfPTable table, Map<String, Object> data) {
        log.info("Inside addRows");
        table.addCell((String) data.get("name"));
        table.addCell((String) data.get("category"));
        table.addCell((String) data.get("quantity"));
        table.addCell(Double.toString((Double) data.get("price")));
        table.addCell(Double.toString((Double) data.get("total")));
    }

    private void addTableHeader(PdfPTable table) {
        log.info("Inside addTableHeader");
        Stream.of("Name", "Category", "Quantity", "Price", "Sub Total")
                .forEach(columnTitle -> {
                    PdfPCell header = new PdfPCell();
                    header.setBackgroundColor(BaseColor.LIGHT_GRAY);
                    header.setBorderWidth(2);
                    header.setPhrase(new Phrase(columnTitle));
                    header.setBackgroundColor(BaseColor.YELLOW);
                    header.setHorizontalAlignment(Element.ALIGN_CENTER);
                    header.setVerticalAlignment(Element.ALIGN_CENTER);
                    table.addCell(header);
                });
    }

    private Font getFont(String type) {
        log.info("Inside getFont");
        Font font;
        switch (type) {
            case "Header":
                font = FontFactory.getFont(FontFactory.HELVETICA, 18, BaseColor.BLACK);
                font.setStyle(Font.BOLD);
            case "Data":
                font = FontFactory.getFont(FontFactory.TIMES_ROMAN, 11, BaseColor.BLACK);
                font.setStyle(Font.BOLD);
            default:
                font = new Font();
        }
        return font;
    }

    private void setRectangleInPdf(Document document) throws DocumentException {
        log.info("Inside setRectangleInPdf");
        Rectangle rectangle = new Rectangle(577, 825, 18, 15);
        rectangle.enableBorderSide(1);
        rectangle.enableBorderSide(2);
        rectangle.enableBorderSide(4);
        rectangle.enableBorderSide(8);
        rectangle.setBorderColor(BaseColor.BLACK);
        rectangle.setBorderWidth(1);
        document.add(rectangle);
    }

    private void insertBill(Map<String, Object> requestMap) {
        try {
            Bill bill = new Bill();
            bill.setUuid((String) requestMap.get("uuid"));
            bill.setName((String) requestMap.get("name"));
            bill.setEmail((String) requestMap.get("email"));
            bill.setContactNumber((String) requestMap.get("contactNumber"));
            bill.setPaymentMethod((String) requestMap.get("paymentMethod"));
            bill.setTotal(Integer.parseInt((String) requestMap.get("totalAmount")));
            bill.setProductDetails((String) requestMap.get("productDetails"));
            bill.setCreatedBy(jwtFilter.getCurrentUser());
            billDao.save(bill);
        } catch (Exception ex) {
            ex.printStackTrace();
        }

    }

    private boolean validateRequestMap(Map<String, Object> requestMap) {
        return requestMap.containsKey("name")
                && requestMap.containsKey("contactNumber")
                && requestMap.containsKey("email")
                && requestMap.containsKey("paymentMethod")
                && requestMap.containsKey("productDetails")
                && requestMap.containsKey("totalAmount");
    }
}
