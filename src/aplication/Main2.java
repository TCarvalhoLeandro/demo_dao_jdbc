package aplication;

import java.util.ArrayList;
import java.util.List;

import model.dao.DaoFactory;
import model.dao.DepartmentDao;
import model.entities.Department;

public class Main2 {

	public static void main(String[] args) {
		
		DepartmentDao depDao = DaoFactory.createDepartmentDao();
		
		System.out.println(">>>>>> TESTE 1: Department insert <<<<<<");
		Department newDep = new Department(0, "Food");
		depDao.insert(newDep);
		System.out.println("Inserido! Id: " + newDep.getId());
		
		
		System.out.println();
		System.out.println(">>>>>> TESTE 2: Department findById <<<<<<");
		Department dep = depDao.findById(2);
		System.out.println(dep);
		
		
		System.out.println();
		System.out.println(">>>>>> TESTE 3: Department Update <<<<<<");
		dep = depDao.findById(1);// pega o objeto do banco e monta em 'dep'
		dep.setId(10);
		dep.setName("Informatica");
		depDao.update(dep);
		System.out.println("Atualização concluida!");
		
		
		System.out.println();
		System.out.println(">>>>>> TESTE 4: Department findAll <<<<<<");
		List<Department> listDep = new ArrayList<>();
		listDep = depDao.findAll();
		for(Department obj: listDep) {
			System.out.println(obj);
		}
		
		
		
		System.out.println();
		System.out.println(">>>>>> TESTE 5: Department delete <<<<<<");
		depDao.deleteById(9);
		
	}

}
