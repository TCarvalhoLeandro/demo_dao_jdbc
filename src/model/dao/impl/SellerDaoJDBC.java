package model.dao.impl;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
		PreparedStatement st = null;
		try {
			st = conn.prepareStatement("INSERT INTO seller "
									 + "(Name, Email, BirthDate, BaseSalary, DepartmentId) "
									 + "VALUES "
									 + "(?, ?, ?, ?, ?)", Statement.RETURN_GENERATED_KEYS);
			st.setString(1, obj.getName());
			st.setString(2, obj.getEmail());
			st.setDate(3, new java.sql.Date(obj.getBirthDate().getTime()));
			st.setDouble(4, obj.getBaseSalary());
			st.setInt(5, obj.getDepartment().getId());
			
			int linhasConstruidas =  st.executeUpdate();
			
			if(linhasConstruidas > 0) {
				ResultSet rs = st.getGeneratedKeys();
				if(rs.next()) {
					int id = rs.getInt(1);
					obj.setId(id);
				}
				DB.closeResultSet(rs);
			}
			else {
				throw new DbException("Erro inexperado. Nenhuma linha construida.");
			}
		}
		catch(SQLException e) {
			throw new DbException(e.getMessage());
		}
		finally {
			DB.closeStatement(st);
		}
		
	}

	@Override
	public void update(Seller obj) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void deleteById(Integer id) {
		// TODO Auto-generated method stub
		
	}
	
	// BUSCA POR ID-SELLER ===========================================================================================
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

	// BUSCA TODOS OS SELLERS ==============================================================================================
	@Override
	public List<Seller> findAll() {
		// Declaração dos recursos fora do try para fechar no finally
		PreparedStatement st = null;
		ResultSet rs = null;
		
		try {
			// 1. PREPARAÇÃO DO SQL
	        // Seleciona vendedores e o nome do departamento (para preencher o objeto Department depois)
			
			st = conn.prepareStatement("SELECT seller.*,department.Name as DepName "
					+ "FROM seller INNER JOIN department "// Junta as tabelas
					+ "ON seller.DepartmentId = department.Id "// Onde a chave estrangeira bate com a primária
					+ "ORDER BY Name");// Ordena a lista final por nome alfabético
			
			
			// Executa a query e recebe a tabela virtual de resultados
			rs = st.executeQuery();
			
			// Cria a lista vazia que será retornada no final
			List<Seller> list = new ArrayList<>();
			
			// TRUQUE DO MAP (Controle de Unicidade):
	        // Cria um mapa vazio para guardar os Departamentos que já instanciamos.
	        // Chave: ID do departamento (Integer) -> Valor: Objeto Department
			Map<Integer, Department> map = new HashMap<>();
			
			// Loop 'while': Diferente do 'if(rs.next())', usamos 'while' porque
	        // esperamos VÁRIOS resultados (uma lista de vendedores), não apenas um.
			while (rs.next()) {
				
				// Verifica no Map se já existe um Department com o ID dessa linha atual
				Department dep = map.get(rs.getInt("DepartmentId"));
				
				// Se 'dep' for nulo, significa que é a primeira vez que encontramos esse departamento
				if(dep == null) {
					// Instancia o departamento usando os dados do banco
					dep = instantiateDepartment(rs);
					
					// Salva no map para que na próxima volta do loop a gente não crie outro igual
					map.put(rs.getInt("DepartmentId"), dep);
				}
				
				// Instancia o Vendedor, passando o objeto 'dep'.
	            // Note que se for o 2º, 3º ou 10º vendedor, o 'dep' será O MESMO objeto da memória
	            // recuperado pelo map.get(), e não uma nova instância duplicada.
				Seller seller = instantiateSeller(rs, dep);
				
				// Adiciona o vendedor pronto na lista final
				list.add(seller);
				
			}
			// Retorna a lista preenchida
			return list;
		}
		catch(SQLException e) {
			// Trata erros de SQL lançando exceção personalizada
			throw new DbException(e.getMessage());
		}
		finally {
			// Fecha recursos para evitar vazamento de memória
			DB.closeStatement(st);
			DB.closeResultSet(rs);
		}
	}
	
	// BUSCA POR ID-DEPARTMENT =====================================================================================
	@Override
	public List<Seller> findByDepartment(Department department) {
		
		// Declaração dos recursos fora do try para fechar no finally
		PreparedStatement st = null;
		ResultSet rs = null;
		
		try {
			// 1. PREPARAÇÃO DO SQL
	        // Seleciona vendedores e o nome do departamento (para preencher o objeto Department depois)
			
			st = conn.prepareStatement("SELECT seller.*,department.Name as DepName "
					+ "FROM seller INNER JOIN department "// Junta as tabelas
					+ "ON seller.DepartmentId = department.Id "// Onde a chave estrangeira bate com a primária
					+ "WHERE DepartmentId = ? "// Filtra: queremos só os vendedores DESTE departamento
					+ "ORDER BY Name");// Ordena a lista final por nome alfabético
			
			// Substitui o '?' pelo ID do departamento que veio no parâmetro do método
			st.setInt(1, department.getId());
			
			// Executa a query e recebe a tabela virtual de resultados
			rs = st.executeQuery();
			
			// Cria a lista vazia que será retornada no final
			List<Seller> list = new ArrayList<>();
			
			// TRUQUE DO MAP (Controle de Unicidade):
	        // Cria um mapa vazio para guardar os Departamentos que já instanciamos.
	        // Chave: ID do departamento (Integer) -> Valor: Objeto Department
			Map<Integer, Department> map = new HashMap<>();
			
			// Loop 'while': Diferente do 'if(rs.next())', usamos 'while' porque
	        // esperamos VÁRIOS resultados (uma lista de vendedores), não apenas um.
			while (rs.next()) {
				
				// Verifica no Map se já existe um Department com o ID dessa linha atual
				Department dep = map.get(rs.getInt("DepartmentId"));
				
				// Se 'dep' for nulo, significa que é a primeira vez que encontramos esse departamento
				if(dep == null) {
					// Instancia o departamento usando os dados do banco
					dep = instantiateDepartment(rs);
					
					// Salva no map para que na próxima volta do loop a gente não crie outro igual
					map.put(rs.getInt("DepartmentId"), dep);
				}
				
				// Instancia o Vendedor, passando o objeto 'dep'.
	            // Note que se for o 2º, 3º ou 10º vendedor, o 'dep' será O MESMO objeto da memória
	            // recuperado pelo map.get(), e não uma nova instância duplicada.
				Seller seller = instantiateSeller(rs, dep);
				
				// Adiciona o vendedor pronto na lista final
				list.add(seller);
				
			}
			// Retorna a lista preenchida
			return list;
		}
		catch(SQLException e) {
			// Trata erros de SQL lançando exceção personalizada
			throw new DbException(e.getMessage());
		}
		finally {
			// Fecha recursos para evitar vazamento de memória
			DB.closeStatement(st);
			DB.closeResultSet(rs);
		}
	}

}
