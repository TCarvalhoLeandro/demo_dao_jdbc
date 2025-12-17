package model.dao.impl;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;

import java.util.List;


import db.DB;
import db.DbException;
import model.dao.DepartmentDao;
import model.entities.Department;



public class DepartmentDaoJDBC implements DepartmentDao{
	
    private Connection conn; // depedencia de conexao com banco
	
	public DepartmentDaoJDBC(Connection conn) {// injeção de dependencia no construtor
		this.conn = conn;
	}
	

	//INSERIR NA TABELA ================================================================================================
	@Override
	public void insert(Department obj) {
		PreparedStatement st = null;
		try {
			// 1. PREPARAR O SQL DE INSERÇÃO
	        // Atenção ao segundo parâmetro: Statement.RETURN_GENERATED_KEYS
	        // Ele avisa ao JDBC: "Vou inserir dados, e quero que você traga
			// de volta a chave primária (ID) que o banco criar".
			st = conn.prepareStatement("INSERT INTO department "
									 + "(Id, Name) "
									 + "VALUES "
									 + "(?, ?)", Statement.RETURN_GENERATED_KEYS);
			
			// 2. DEFINIR OS VALORES (Substituir os '?')
			st.setInt(1, obj.getId());
			st.setString(2, obj.getName());
			
			// 3. EXECUTAR A INSERÇÃO
	        // executeUpdate() altera o banco e retorna quantos registros foram afetados.
	        // Esperamos que retorne 1 (uma linha criada).
			int linhasConstruidas =  st.executeUpdate();

			// 4. RECUPERAR O ID GERADO
			if(linhasConstruidas > 0) {
				ResultSet rs = st.getGeneratedKeys();// Pega o ResultSet especial contendo as chaves geradas
				
				// Se existir uma chave lá dentro...
				if(rs.next()) {
					// Pega o valor da primeira coluna desse ResultSet (que é o novo ID)
					int id = rs.getInt(1);
					
					// ATUALIZAÇÃO DO OBJETO:
	                // O objeto 'obj' que veio como argumento não tinha ID (era null).
	                // Agora setamos o ID nele para que o objeto na memória fique igual ao do banco.
	                obj.setId(id);
				
				}
				DB.closeResultSet(rs);// Fecha este ResultSet auxiliar
			}
			else {
				// Se executeUpdate retornou 0, algo estranho aconteceu e nada foi salvo.
				throw new DbException("Erro inexperado. Nenhuma linha construida.");
			}
		}
		catch(SQLException e) {
			throw new DbException(e.getMessage());
		}

	}

	// ATUALIZAR POR ID ==================================================================================================
	@Override
	public void update(Department obj) {
		PreparedStatement st = null;
		
		try {
			// 1. PREPARAR O SQL DE ATUALIZAÇÃO
	        // Atenção ao segundo parâmetro: Statement.RETURN_GENERATED_KEYS
	        // Ele avisa ao JDBC: "Vou inserir dados, e quero que você traga 
			// de volta a chave primária (ID) que o banco criar"
			st = conn.prepareStatement("UPDATE department "
					  + "SET Id = ?, Name = ? "
					  + "WHERE Id = ?");
			
			// 2. DEFINIR OS VALORES (Substituir os '?')
			st.setInt(1, obj.getId());
			st.setString(2, obj.getName());
			st.setInt(3, obj.getId());
			
			st.executeUpdate();
			
		}
		catch(SQLException e) {
			throw new DbException(e.getMessage());
		}
		finally {
			DB.closeStatement(st);
		}
		
	}
	
	// DELETAR POR ID ========================================================================================
	@Override
	public void deleteById(Integer id) {
		// Declara o PreparedStatement fora do try para garantir o fechamento no finally
		PreparedStatement st = null;
		
		try {
			// 1. PREPARAR O SQL DE REMOÇÃO
	        // O comando é direto: Apague da tabela seller ONDE o Id for igual ao parâmetro.
	        // O '?' é fundamental para evitar SQL Injection.
			st = conn.prepareStatement("DELETE FROM department WHERE Id = ?");
			
			// 2. DEFINIR O PARÂMETRO
	        // Substitui o '?' pelo valor do ID recebido no método.
			st.setInt(1, id);
			
			
			// 3. EXECUTAR A REMOÇÃO
	        // Usamos 'executeUpdate()' porque é uma operação que altera o banco (não é uma consulta).
	        // Diferente do Insert, aqui não precisamos verificar o retorno (int) se não quisermos validar
	        // se o ID realmente existia. O comando roda e, se o ID existir, apaga; se não, nada acontece.
			st.executeUpdate();
		}
		catch(SQLException e) {
			throw new DbException(e.getMessage());
		}
		finally {
			DB.closeStatement(st);
		}
		
	}

	// BUSCAR POR ID =====================================================================================
	@Override
	public Department findById(Integer id) {
		// Declara st e rs fora do try para que possam ser fechados no bloco 'finally'
		PreparedStatement st = null;
		ResultSet rs = null;
		
		try {
			// Prepara o comando SQL. 
	        // O INNER JOIN é essencial para trazer os dados do Vendedor E do Departamento dele numa única busca.
			st = conn.prepareStatement("SELECT * FROM department WHERE Id = ?");// O '?' será substituído pelo ID
			
			
			st.setInt(1, id);// Define o valor do ID na posição do '?'
			
			rs = st.executeQuery();// Executa a consulta no banco e guarda o resultado na tabela virtual 'rs'
		
			if(rs.next()) {// Testa se veio algum resultado (move o cursor para a primeira linha)
				
				Department dep = new Department();
				
				dep.setId(rs.getInt("Id"));
				dep.setName(rs.getString("Name"));
				
				return dep;// Retorna o objeto completo montado
				
			}
			return null;// Se não entrou no if, não achou ninguém com esse ID
		}
		catch(SQLException e) {
			throw new DbException(e.getMessage());
		}
		finally {
			DB.closeResultSet(rs);
		}
	}
	
	// LISTAR TODOS ===================================================================================
	@Override
	public List<Department> findAll() {
		// Declaração dos recursos fora do try para fechar no finally
		PreparedStatement st = null;
		ResultSet rs = null;
		
		try {
			// 1. PREPARAÇÃO DO SQL
	        // Seleciona vendedores e o nome do departamento (para preencher o objeto Department depois)
			
			st = conn.prepareStatement("SELECT * FROM department ORDER BY Name");// buscar todas as linhas
			
			
			// Executa a query e recebe a tabela virtual de resultados
			rs = st.executeQuery();
			
			// Cria a lista vazia que será retornada no final
			List<Department> list = new ArrayList<>();
			
			
			// Loop 'while': Diferente do 'if(rs.next())', usamos 'while' porque
	        // esperamos VÁRIOS resultados (uma lista de vendedores), não apenas um.
			while (rs.next()) {
				Department dep = new Department();
				
				dep.setId(rs.getInt("Id"));
				dep.setName(rs.getString("Name"));				
				list.add(dep);
				
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


	public Connection getConn() {
		return conn;
	}


	public void setConn(Connection conn) {
		this.conn = conn;
	}

}
