package model.dao;

import model.dao.impl.SellerDaoJDBC;

// CLASSE RESPONSAVEL POR FAZER AS INSTANCIACOES DE DAO

public class DaoFactory {

	// Para nao expor a implementacao, esse metodo retorna a interface SellerDao mas internamente instancia SllerDaoJDBC 
	public static SellerDao createSellerDao() {
		return new SellerDaoJDBC();
	}
}
