package aplication;

import java.util.Date;

import model.entities.Department;
import model.entities.Seller;

public class Main {

	public static void main(String[] args) {
		
		Department dep = new Department(1,"Books");
		System.out.println(dep);
		
		Seller seller = new Seller(21, "Bob", "bob@gmail.com", new Date(), 2000.0, dep);
		System.out.println(seller);

	}

}
