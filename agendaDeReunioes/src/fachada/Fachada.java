package fachada;
/**********************************
 * IFPB - Curso Superior de Tec. em Sist. para Internet
 * Programação Orientada a Objetos
 * Prof. Fausto Maranhão Ayres
 **********************************/

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Properties;
import java.util.Scanner;
import java.util.TreeMap;

import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;

import java.lang.Iterable;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

import modelo.Participante;
import modelo.Reuniao;
import repositorio.Repositorio;



public class Fachada {
	private static Repositorio repositorio = new Repositorio();	//existe somente um repositorio

	public static ArrayList<Participante> listarParticipantes() {
		return repositorio.getParticipantes();
	}
	public static ArrayList<Reuniao> listarReunioes() {
		return repositorio.getReunioes();
	}

	public static Participante criarParticipante(String nome, String email) throws Exception {
		nome = nome.trim();
		email = email.trim();
		Participante p = repositorio.localizarParticipante(nome);
		if(p != null) {
			throw new Exception("Participante existente");
		}
		p = new Participante(nome, email);
		repositorio.adicionar(p);
		return p;
	}

	public static Reuniao criarReuniao (String datahora, String assunto, ArrayList<String> nomes) throws Exception {
		assunto = assunto.trim();
		int id = 1;
		
		
		DateTimeFormatter dt = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
		LocalDateTime da = LocalDateTime.parse(datahora, dt);

		if(nomes.size() < 2)
			throw new Exception ("Reunão menor de 2 participantes");
		ArrayList<Participante> part = new ArrayList<>();
		
		for(String n : nomes) {
			Participante p = repositorio.localizarParticipante(n);
			if(p == null)
				throw new Exception("participante" + n + "inexistente");
			else 
				part.add(p);
		}
		
		Reuniao r = new Reuniao(id,da,assunto);
		for(Participante p: part) {
			ArrayList<Reuniao> reulista = p.getReunioes();
			if(reulista != null && !reulista.isEmpty()) {
				for(int i = 0; i< reulista.size(); i++) {
					if (reulista.get(i).getDatahora().equals(da))
						throw new Exception("participante já está em outra reunião no mesmo horário");
					LocalDateTime inicio = reulista.get(i).getDatahora();
					Duration horario = Duration.between(inicio, da);
					long diferenca;
					diferenca = horario.toHours();
					if(Math.abs(diferenca) < 2) 
						throw new Exception("participante já está em outra reunião no mesmo horário");
				}
			}
			r.adicionar(p);
		}
	
		repositorio.adicionar(r);
		
		enviarEmail(assunto, "Convidamos você para uma reunião. Sobre: "+ assunto);
		return r;
		
	}


	public static void 	adicionarParticipanteReuniao(String nome, int id) throws Exception {
		nome = nome.trim();
		
		//localizar participante e reuniao no repositorio e adicioná-lo à reunião
		//enviarEmail(emaildestino, assunto, mensagem)
		Participante p = repositorio.localizarParticipante(nome);
		Reuniao r = repositorio.localizarReuniao(id);
		if(p==null )
			throw new Exception("nao pode adicionar - participante inexistente");
		if( r == null)
			throw new Exception("nao pode adicionar - participante inexistente");
		if(r.localizarParticipante(nome) == p)
			throw new Exception("nao pode adicionar - participante já existente na Reunião");
		
		r.adicionar(p);
		
		enviarEmail(r.getAssunto(), "Convidamos você para uma reunião. Sobre: "+ r.getAssunto());
		
	}
	
	public static void 	removerParticipanteReuniao(String nome, int id) throws Exception {
		nome = nome.trim();
		
		//localizar participante e reuniao no repositorio e removê-lo da reunião
		//enviarEmail(emaildestino, assunto, mensagem)
		Participante p = repositorio.localizarParticipante(nome);
		if(p==null)
			throw new Exception("nao pode remover - participante inexistente:" + nome);

		Reuniao r = repositorio.localizarReuniao(id);
		if(r==null)
			throw new Exception("nao pode remover - reuniao inexistente:" + id);

		r.remover(p);
		if(r.getTotalParticipantes() < 2) {
			cancelarReuniao(id);
			enviarEmail(r.getAssunto(), "Desculpa, mas você foi removido da presente reunião."+ r.getAssunto());
			throw new Exception("Reunião foi cancelada. Pedimos total desculpas pelo ocorrido");
		}
		enviarEmail(r.getAssunto(), "Desculpa, mas você foi removido da presente reunião."+ r.getAssunto());
		
		
	}
	public static void	cancelarReuniao(int id) throws Exception {
		//localizar a reunião no repositório, removê-la de seus participantes e
		//removê-la do repositorio
		//enviarEmail(emaildestino, assunto, mensagem)
		
		Reuniao r = repositorio.localizarReuniao(id);
		if(r==null )
			throw new Exception("nao pode cancelar a reunião - reuniao inexistente");
		for(Participante p : r.getParticipantes()) {
			if(p.getReunioes().contains(r))
				p.remover(r);
		}
		
		repositorio.remover(r);
		enviarEmail(r.getAssunto(), "Desculpa, mas a presente reunião foi cancelada."+ r.getAssunto());
		
		throw new Exception("A presente reunião" + id + "foi cancelada.");
		
		
	}

	public static void inicializar() throws Exception {
		//ler dos arquivos textos (formato anexo) os dados dos participantes e 
		//das reuniões e adicioná-los ao repositório

		Scanner arquivo1=null;
		Scanner arquivo2=null;
		try{
			arquivo1 = new Scanner( new File("src/arquivos/participantes.txt"));
		}catch(FileNotFoundException e){
			throw new Exception("arquivo de participantes inexistente:");
		}
		try{
			arquivo2 = new Scanner( new File("src/arquivos/reunioes.txt"));
		}catch(FileNotFoundException e){
			throw new Exception("arquivo de reunioes inexistente:");
		}

		String linha;	
		String[] partes;	
		String nome, email;
		while(arquivo1.hasNextLine()) {
			linha = arquivo1.nextLine().trim();		
			partes = linha.split(";");	
			nome = partes[0];
			email = partes[1];
			Participante p = new Participante(nome,email);
			repositorio.adicionar(p);
		} 
		arquivo1.close();			

		String id,datahora, assunto;
		String[] nomes;
		while(arquivo2.hasNextLine()) {
			linha = arquivo2.nextLine().trim();		
			partes = linha.split(";");	
			id = partes[0];
			datahora = partes[1];
			assunto = partes[2];
			nomes = partes[3].split(",");
			ArrayList<Participante> listapresentes = new ArrayList<>();
			DateTimeFormatter parser = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
			LocalDateTime dt = LocalDateTime.parse(datahora, parser);		
			//Reuniao r = new Reuniao(Integer.parseInt(id), dt, assunto);
			for(String n : nomes){
				Participante p = repositorio.localizarParticipante(n);
				listapresentes.add(p);
			}
			Reuniao r = new Reuniao(Integer.parseInt(id), dt, assunto);
			for(Participante p: listapresentes) {
				r.adicionar(p);
			}
			repositorio.adicionar(r);
		} 
		arquivo2.close();
	}

	public static void	finalizar() throws Exception{
		//gravar nos arquivos textos  os dados dos participantes e 
		//das reuniões que estão no repositório
		
		FileWriter arquivo1=null;
		FileWriter arquivo2=null;
		try{
			arquivo1 = new FileWriter( new File("src/arquivos/participantes.txt") ); 
		}catch(IOException e){
			throw new Exception("problema na criação do arquivo de participantes");
		}
		try{
			arquivo2 = new FileWriter( new File("src/arquivos/reunioes.txt") ); 
		}catch(IOException e){
			throw new Exception("problema na criação do arquivo de reunioes");
		}
		
		
		for(Participante p : repositorio.getParticipantes()) {
			arquivo1.write(p.getNome() +";" + p.getEmail() +"\n");	
		} 
		arquivo1.close();			

		ArrayList<String> lista;
		String nomes;
		for(Reuniao r : repositorio.getReunioes()) {
			lista = new ArrayList<>();
			String dt = LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy hh:mm"));
			for(Participante p : r.getParticipantes()) {
				lista.add(p.getNome());
			}
			nomes = String.join(",", lista);
			arquivo2.write(r.getId()+";"+dt+";"+r.getAssunto()+";"+nomes+"\n");	
		} 
		arquivo2.close();	

	}
	
	
	
	/**************************************************************
	 * 
	 * MÉTODO PARA ENVIAR EMAIL, USANDO UMA CONTA (SMTP) DO GMAIL
	 * ELE ABRE UMA JANELA PARA PEDIR A SENHA DO EMAIL DO EMITENTE
	 * ELE USA A BIBLIOTECA JAVAMAIL 1.6.2
	 * Lembrar de: 
	 * 1. desligar antivirus e de 
	 * 2. ativar opcao "Acesso a App menos seguro" na conta do gmail
	 * 
	 **************************************************************/
	public static void enviarEmail(String assunto, String mensagem){
		try {
			//configurar emails
			String emailorigem = "renatoxavier.ds@gmail.com";
			String senhaorigem = pegarSenha();
			String emaildestino = "renato.xavier@ifpb.edu.br";
			//String emaildestino = "fausto.ayres@gmail.com";

			//Gmail
			Properties props = new Properties();
			props.put("mail.smtp.starttls.enable", "true");
			props.put("mail.smtp.host", "smtp.gmail.com");
			props.put("mail.smtp.port", "587");
			props.put("mail.smtp.auth", "true");

			Session session;
			session = Session.getInstance(props,
					new javax.mail.Authenticator() {
				protected PasswordAuthentication getPasswordAuthentication() {
					return new PasswordAuthentication(emailorigem, senhaorigem);
				}
			});

			MimeMessage message = new MimeMessage(session);
			message.setSubject(assunto);		
			message.setFrom(new InternetAddress(emailorigem));
			message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(emaildestino));
			message.setText(mensagem);   // usar "\n" para quebrar linhas
			Transport.send(message);

			System.out.println("enviado com sucesso");

		} catch (MessagingException e) {
			System.out.println(e.getMessage());
		}
	}
	
	/*
	 * JANELA PARA DIGITAR A SENHA DO EMAIL
	 */
	public static String pegarSenha(){
		JPasswordField field = new JPasswordField(10);
		field.setEchoChar('*'); 
		JPanel painel = new JPanel();
		painel.add(new JLabel("Entre com a senha do seu email:"));
		painel.add(field);
		JOptionPane.showMessageDialog(null, painel, "Senha", JOptionPane.PLAIN_MESSAGE);
		String texto = new String(field.getPassword());
		return texto.trim();
	}
	
}
