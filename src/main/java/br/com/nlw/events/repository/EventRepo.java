package br.com.nlw.events.repository;

import org.springframework.data.repository.CrudRepository;

import br.com.nlw.events.model.Event;

// modelo e tipo do identificador único da tabela
public interface EventRepo extends CrudRepository<Event, Integer> {

	// pelo nome do evento ele monta a query sql
	// findBy é da documenção, PrettyName é o nome do atributo no método e o método recebe um parâmetro correspondente ao tipo do dado
	public Event findByPrettyName(String preetyName);
	
}
