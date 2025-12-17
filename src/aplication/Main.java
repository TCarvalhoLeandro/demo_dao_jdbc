package aplication;



import java.util.Date;
import java.util.List;

import model.dao.DaoFactory;
import model.dao.SellerDao;
import model.entities.Department;
import model.entities.Seller;

public class Main {

	public static void main(String[] args) {
		
		
		
		SellerDao sellerDao = DaoFactory.createSellerDao();// O programa nao conhece a implementacao, conhece somente a interface
		
		System.out.println(">>>>>> TESTE 1: Seller findById <<<<<<");
		Seller seller = sellerDao.findById(2);
		System.out.println(seller);
		
		System.out.println();
		
		System.out.println(">>>>>> TESTE 2: Seller findByDepartment <<<<<<");
		Department dep = new Department(1, null);
		List<Seller> sellerList = sellerDao.findByDepartment(dep);
		for(Seller obj: sellerList) {
			System.out.println(obj);
		}
		
		System.out.println();
		
		System.out.println(">>>>>> TESTE 3: Seller findAll <<<<<<");
		sellerList = sellerDao.findAll();
		for(Seller obj: sellerList) {
			System.out.println(obj);
		}
		
		System.out.println();
		
		System.out.println(">>>>>> TESTE 4: Seller insert <<<<<<");
		Seller newSeller = new Seller(0, "Greg Black", "gregblack@gmail.com", new Date(), 4000.0, dep);
		sellerDao.insert(newSeller);
		System.out.println("Inserido! Id: " + newSeller.getId());
		
		System.out.println();
		
		System.out.println(">>>>>> TESTE 5: Seller update <<<<<<");
		seller = sellerDao.findById(2);
		seller.setName("Luke Cage");
		seller.setEmail("lukecage@gmail.com");
		sellerDao.update(seller);
		System.out.println("Atualização concluida!");
		
	}

}













