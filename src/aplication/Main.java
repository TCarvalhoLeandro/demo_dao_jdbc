package aplication;



import model.dao.DaoFactory;
import model.dao.SellerDao;

import model.entities.Seller;

public class Main {

	public static void main(String[] args) {
		
		
		
		SellerDao sellerDao = DaoFactory.createSellerDao();// O programa nao conhece a implementacao, conhece somente a interface
		
		Seller seller = sellerDao.findById(2);
		
		System.out.println(seller);

	}

}
