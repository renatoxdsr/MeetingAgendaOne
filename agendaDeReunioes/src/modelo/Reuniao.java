package modelo;
/**********************************
 * IFPB - Curso Superior de Tec. em Sist. para Internet
 * Programação Orientada a Objetos
 * Prof. Fausto Maranhão Ayres
 **********************************/

import java.time.LocalDateTime;
import java.util.ArrayList;


public class Reuniao {
	private int id;
	private LocalDateTime datahora;
	private String assunto;
	private ArrayList<Participante> participantes = new ArrayList<Participante>();
	
	
	
	public Reuniao(int id, LocalDateTime datahora, String assunto) {
		this.id = id;
		this.assunto = assunto;
		this.datahora = datahora;
	}
	public void adicionar(Participante p){
		participantes.add(p);
		p.getReunioes().add(this);
	}
	public void remover(Participante p){
		participantes.remove(p);
		p.getReunioes().remove(this);
	}
	public Participante localizarParticipante(String nome){
		for(Participante p : participantes){
			if(p.getNome().equals(nome))
				return p;
		}
		return null;
	}
	
	public ArrayList<Participante> getParticipantes() {
		return participantes;
	}
	public void  setParticipantes(ArrayList<Participante> Participantes) {
		this.participantes =  participantes;
	}
	public int getTotalParticipantes(){
		return participantes.size();
	}

	public String getAssunto() {
		return assunto;
	}
	public void setAssunto(String assunto) {
		this.assunto = assunto;
	}

	public int getId() {
		return id;
	}
	public void setId(int id) {
		this.id = id;
	}
	
	public LocalDateTime getDatahora() {
		return datahora;
	}
	
	public void setDatahora(LocalDateTime datahora) {
		this.datahora = datahora;
	}
	public Reuniao(int id, LocalDateTime datahora, String assunto, ArrayList<Participante> participantes) {
		super();
		this.id = id;
		this.datahora = datahora;
		this.assunto = assunto;
		this.participantes = participantes;
	}
	
	

	@Override
	public String toString() {
		String texto = "id=" + id + ",horario" + datahora + ", assunto=" + assunto ;
		texto += ", participantes:";
		if (participantes.isEmpty())
			texto += " vazia";
		else 	
			for(Participante p: participantes) 
				texto += " " + p.getNome() ;

		return texto ;
	}


}
	
