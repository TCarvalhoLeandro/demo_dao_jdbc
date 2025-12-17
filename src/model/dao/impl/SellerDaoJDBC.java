package model.dao.impl;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

import db.DB;
import db.DbException;
import model.dao.SellerDao;
import model.entities.Department;
import model.entities.Seller;

// CLASSE RESPONSAVEL POR IMPLEMENTAR O DAO (Data Access Object) DO OBJETO Seller
public class SellerDaoJDBC implements SellerDao{

	private Connection conn; // depedencia de conexao com banco
	
	public SellerDaoJDBC(Connection conn) {// injeção de dependencia no construtor
		this.conn = conn;
	}
	
	@Override
	public void insert(Seller obj) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void update(Seller obj) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void deleteById(Integer id) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public Seller findById(Integer id) {
		// Declara st e rs fora do try para que possam ser fechados no bloco 'finally'
		PreparedStatement st = null;
		ResultSet rs = null;
		try {
			// Prepara o comando SQL. 
	        // O INNER JOIN é essencial para trazer os dados do Vendedor E do Departamento dele numa única busca.
			st = conn.prepareStatement("SELECT seller.*,department.Name as DepName "
					+ "FROM seller INNER JOIN department "
					+ "ON seller.DepartmentId = department.Id "
					+ "WHERE seller.Id = ?");// O '?' será substituído pelo ID
			
			
			st.setInt(1, id);// Define o valor do ID na posição do '?'
			
			rs = st.executeQuery();// Executa a consulta no banco e guarda o resultado na tabela virtual 'rs'
			
			if(rs.next()) {// Testa se veio algum resultado (move o cursor para a primeira linha)
				// AQUI ESTÁ A MÁGICA DA ORGANIZAÇÃO:
	            // Em vez de criar o Department aqui dentro bagunçando o código, 
	            // chamamos um método auxiliar privado que sabe converter um ResultSet em um Department.
				Department dep = instantiateDepartment(rs);
				
				// Mesma coisa para o Seller. Passamos o 'rs' (dados) e o 'dep' (associação).
	            // O Vendedor precisa saber a qual departamento pertence.
				Seller seller = instantiateSeller(rs, dep);
				
				return seller;// Retorna o objeto completo montado
				
			}
			return null;// Se não entrou no if, não achou ninguém com esse ID
		}
		catch(SQLException e) {
			throw new DbException(e.getMessage());// Captura erros do SQL e relança como uma exceção personalizada da sua arquitetura
		}
		finally {
			// Fecha os recursos para não travar o banco de dados
			DB.closeStatement(st);
			DB.closeResultSet(rs);
		}
		
		
	}
	// Método auxiliar (privado) para não repetir código de criar Departamento
	private Department instantiateDepartment(ResultSet rs) throws SQLException{
		Department dep = new Department();
		
		dep.setId(rs.getInt("DepartmentId"));// Pega o valor da coluna "DepartmentId" do banco e joga no objeto Java
		
		dep.setName(rs.getString("DepName"));// Pega o valor da coluna apelidada de "DepName" no SQL (veja o SELECT lá em cima)
		return dep;
	}
	
	// Método auxiliar (privado) para criar Seller
	private Seller instantiateSeller(ResultSet rs, Department dep) throws SQLException{
		Seller seller = new Seller();
		
		// Mapeia coluna por coluna do banco para os atributos da classe
		seller.setId(rs.getInt("Id"));
		seller.setName(rs.getString("Name"));
		seller.setEmail(rs.getString("Email"));
		
		// O JDBC converte automaticamente o DATE do SQL para o java.sql.Date ou java.util.Date
		seller.setBirthDate(rs.getDate("BirthDate"));
		seller.setBaseSalary(rs.getDouble("BaseSalary"));
		
		// ASSOCIAÇÃO: Aqui o objeto Seller recebe o objeto Department inteiro.
	    // Isso significa que, na memória, dentro do objeto 'seller' existe um objeto 'dep'.
		seller.setDepartment(dep);
		
		return seller;
	}

	@Override
	public List<Seller> findAll() {
		// TODO Auto-generated method stub
		return null;
	}
	
	

}
