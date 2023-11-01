package br.sp.tgsouza.rest.tests;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;

import java.util.HashMap;
import java.util.Map;


import org.junit.Before;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;

import br.sp.tgsouza.rest.core.BaseTest;
import br.sp.tgsouza.utils.DataUtils;
import io.restassured.RestAssured;

@FixMethodOrder(MethodSorters.NAME_ASCENDING)

public class BarrigaTest extends BaseTest {
	
	private String TOKEN;
	private static String CONTA_NAME = "Conta " + System.nanoTime();
	private static Integer CONTA_ID;
	private static Integer MOV_ID;
	
	@Before
	
	public void login() {
		Map<String, String> login = new HashMap<>();
		login.put("email", "tgsouza88@gmail.com");
		login.put("senha", "123456");
		TOKEN = given()
			.body(login)
		.when()
			.post("/signin")
		.then()
			
			.statusCode(200)
			.extract().path("token");
	}
	
	@Test
	
	public void t01_naoDeveAcessarAPISemToken() {
		given()
		.when()
			.get("/contas")
		.then()
			.statusCode(401);
		
	}
	
	@Test
	
	public void t02_deveIncluirContaComSucesso() {
		
		CONTA_ID = given()
		
		.header("Authorization", "JWT " + TOKEN)
				.body("{\"nome\":\""+CONTA_NAME+"\"}")
			.when()
				.post("/contas")
			.then()
			.statusCode(201)
			.extract().path("id")
		;
	}
	
@Test
	
	public void t03_deveAlterarContaComSucesso() {
			
		given()
		
		.header("Authorization", "JWT " + TOKEN)
				.body("{\"nome\":\""+CONTA_NAME+"alterada\"}")
				.pathParam("id", CONTA_ID)
				.when()
				.put("/contas/{id}")
			.then()
			.statusCode(200)
			//.body("nome", is ("conta alterada"))
		;
	}
@Test

public void t04_naoDeveInserirContaComMesmoNome() {
		
	given()
	
	.header("Authorization", "JWT " + TOKEN)
			.body("{\"nome\":\""+CONTA_NAME+"alterada\"}")
		.when()
			.post("/contas")
		.then()
		.statusCode(400)		
		.body("error", is("Já existe uma conta com esse nome!"))
	;
}
@Test

public void t05_deveInserirMovimentacaoSucesso() {
	Movimentacao mov =  getMovimentacaoValida();

	MOV_ID = given()
	
	.header("Authorization", "JWT " + TOKEN)
			.body(mov)
		.when()
			.post("/transacoes")
		.then()
		.statusCode(201)
		.extract().path("id")
		;
}

@Test

public void t06_deveValidarCamposObrigatoriosMovimentacao() {

	given()
	
	.header("Authorization", "JWT " + TOKEN)
			.body("{}")
		.when()
			.post("/transacoes")
		.then()
		.statusCode(400)
		.body("$", hasSize(8))
		.body("msg", hasItems(
				"Data da Movimentação é obrigatório",
				"Data do pagamento é obrigatório",
				"Descrição é obrigatório",
				"Interessado é obrigatório",
				"Valor é obrigatório",
				"Valor deve ser um número",
				"Conta é obrigatório",
				"Situação é obrigatório"
				
				
				))
		;
}
@Test

public void t07_naoDeveInserirMovimentacaoComDataFutura() {
	Movimentacao mov = getMovimentacaoValida();
	mov.setData_transacao(DataUtils.getDataDiferencaDias(2));
	given()
	
	.header("Authorization", "JWT " + TOKEN)
			.body(mov)
		.when()
			.post("/transacoes")
		.then()
		.statusCode(400)
		.body("$", hasSize(1))
		.body("msg", hasItem("Data da Movimentação deve ser menor ou igual à data atual"))
		;
}
@Test
public void t08_naoDeveRemoverContaComMovimentacao() {

	given()
	
	.header("Authorization", "JWT " + TOKEN)
			.pathParam("id", CONTA_ID)
		.when()
			.delete("/contas/{id}")
		.then()
		.statusCode(500)
		.body("constraint", is("transacoes_conta_id_foreign"))
		
		;
}
@Test
public void t09_deveCalcularSaldoContas() {

	given()
	
	.header("Authorization", "JWT " + TOKEN)
			
		.when()
			.get("/saldo")
		.then()
		.statusCode(200)
		.body("find{it.conta_id =="+CONTA_ID+"}.saldo", is("100.00"))
		
		;
}

@Test
public void t10_deveRemoverMovimentacao() {

	given()
	
	.header("Authorization", "JWT " + TOKEN)
	.pathParam("id", MOV_ID)		
		.when()
			.delete("/transacoes/{id}")
		.then()
		.statusCode(204)
		//1832084		
		;
}

private Movimentacao getMovimentacaoValida() {
	Movimentacao mov = new Movimentacao();
	mov.setConta_id(CONTA_ID);
	//mov.setUsuario_id(APP_PORT);
	mov.setDescricao("Descricao da movimentacao");
	mov.setEnvolvido("Envolvido na mov");
	mov.setTipo("REC");
	mov.setData_transacao(DataUtils.getDataDiferencaDias(-1));
	mov.setData_pagamento(DataUtils.getDataDiferencaDias(5));
	mov.setValor(100f);
	mov.setStatus(true);
	return mov;
}

}






	
	
	
	
	
