package modelo;

import java.util.ArrayList;

/**********************************
 * IFPB - Curso Superior de Tec. em Sist. para Internet
 * Programa��o Orientada a Objetos
 * Prof. Fausto Maranh�o Ayres
 **********************************/

public class Participante {

	private String nome;
	private String email;
	private ArrayList<Reuniao> reunioes = new ArrayList<Reuniao>();

	public Participante(String nome,  String email) {
		this.nome = nome;
		this.email = email;
	}
	public String getNome() {
		return nome;
	}
	public void setNome(String nome) {
		this.nome = nome;
	}
	public String getEmail() {
		return email;
	}
	public void setEmail(String email) {
		this.email = email;
	}		

	//---------------------------------------
	public void adicionar(Reuniao r) {
		reunioes.add(r);
		r.getParticipantes().add(this);
	}
	public void remover(Reuniao r) {
		reunioes.remove(r);
	}
	
	public ArrayList<Reuniao> getReunioes() {
		return reunioes;
	}
	public void setReunioes(ArrayList<Reuniao> reunioes) {
		this.reunioes = reunioes;
	}

	public int getTotalReunioes() {
		return reunioes.size();
	}
	
	@Override
	public String toString() {
		String texto = "nome=" + nome + ",Email=" + email + ", Reuni�o" + reunioes;
		if (reunioes != null) {
			if(reunioes.isEmpty())
			texto += "participante sem reuni�o cadastrada";
			else
				
				for(Reuniao r: reunioes) {
					if(r != null)
						texto += ": "+ r.getId();
					}
			return texto;
		}
		return texto;
	
	}
		
}
