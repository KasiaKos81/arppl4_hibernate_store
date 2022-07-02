package pl.sda.arppl4.hibernatestore.parser;

import pl.sda.arppl4.hibernatestore.dao.ProductDao;
import pl.sda.arppl4.hibernatestore.model.Product;
import pl.sda.arppl4.hibernatestore.model.ProductUnit;

import java.time.DateTimeException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;
import java.util.Scanner;

public class ProductCommandLineParser {
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    private final Scanner scanner;
    private final ProductDao dao;

    public ProductCommandLineParser(Scanner scanner, ProductDao dao) {
        this.scanner = scanner;
        this.dao = dao;
    }

    public void parse() {
        String command;

        do {
            System.out.println("Command: add, list, delete, update / quit to finish ");
            command = scanner.next();

            if (command.equalsIgnoreCase("add")){
                handleAddCommand();
            } else if (command.equalsIgnoreCase("list")){
                handleListCommand();
            } else if (command.equalsIgnoreCase("delete")){
                handleDeleteCommand();
            } else if (command.equalsIgnoreCase("update")){
                handleUpdateCommand();
            }

        } while (!command.equals("quit"));

    }

    private void handleUpdateCommand() {
        System.out.println("Provide the id of the product you want to update");
        Long id = scanner.nextLong();

        Optional<Product> productOptional = dao.zwrocProduct(id);
        if (productOptional.isPresent()) {
            Product product = productOptional.get();

            System.out.println("tell what you want to change: price, expiryDate, quantity");
            String change = scanner.next();

            switch (change){
                case "name":
                    System.out.println("provide price");
                    Double price = scanner.nextDouble();
                    product.setPrice(price);
                    break;
                case "expriryDate":
                    LocalDate expiryDate = loadExpiryDateFromUser();
                    product.setExpiryDate(expiryDate);
                    break;
                case "quantity":
                    System.out.println("Provide quantity");
                    Double quantity = scanner.nextDouble();
                    product.setQuantity(quantity);
                    break;
                default:
                    System.out.println("field with this name is not handled");
            }
            dao.updateProduct(product);
            System.out.println("product has been updated");
        } else {
            System.out.println("404 not found");
        }
    }

    private void handleDeleteCommand() {
        System.out.println("Provide the id of the product you want to delete");
        Long id = scanner.nextLong();

        Optional<Product> productOptional = dao.zwrocProduct(id);
        if(productOptional.isPresent()){
        Product product = productOptional.get();
        dao.usunProduct(product);
        System.out.println("removed");
    } else {
        System.out.println("404 not found");
    }}

    private void handleListCommand() {
        List<Product> productList = dao.zwrocListeProducts();
        for (Product product : productList) {
            System.out.println(product);
        }
        System.out.println();
    }

    private void handleAddCommand() {
        System.out.println("Provide name:");
        String name = scanner.next();

        System.out.println("Provide price:");
        Double price = scanner.nextDouble();

        System.out.println("Provide producent");
        String producent = scanner.next();

        LocalDate expiryDate = loadExpiryDateFromUser();

        System.out.println("Provide quantity:");
        Double quantity = scanner.nextDouble();

        ProductUnit productUnit = loadProductUnitFromUser();

        Product product = new Product(null, name, price, producent, expiryDate, quantity, productUnit);
        dao.dodajProduct(product);
    }

    private ProductUnit loadProductUnitFromUser() {
        ProductUnit productUnit = null;
        do {
            try {
                System.out.println("Provide unit:");
                String unitString = scanner.next();

                productUnit = ProductUnit.valueOf(unitString.toUpperCase());
            } catch (IllegalArgumentException iae) {
                System.err.println("wrong unit, please provide unit from: enum");
            }
        } while (productUnit == null);
        return productUnit;
    }

    private LocalDate loadExpiryDateFromUser() {
        LocalDate expiryDate = null;
        do {
            try {
                System.out.println("Provide expiry date:");
                String expiryDateString = scanner.next();
         //       DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
                expiryDate = LocalDate.parse(expiryDateString, FORMATTER);

                LocalDate today = LocalDate.now();
                if(expiryDate.isBefore(today)){
                    // błąd expiry date jest przed dzisiejszym dniem
                    throw new IllegalArgumentException("Date is before today");
                }

            } catch (IllegalArgumentException | DateTimeException iae) {
                expiryDate = null; // żeby wyczyściło jak ktoś wpisał złą date
                System.err.println("Wrong date formatt, should be yyyy-MM-dd");
            }
        } while (expiryDate == null);

        return expiryDate;
    }
}