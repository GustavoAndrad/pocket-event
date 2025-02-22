package br.com.nlw.events.service;

import java.util.List;
import java.util.stream.IntStream;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import br.com.nlw.events.dto.SubscriptionRankingByUser;
import br.com.nlw.events.dto.SubscriptionRankingItem;
import br.com.nlw.events.dto.SubscriptionResponse;
import br.com.nlw.events.exceptions.EventNotFoundException;
import br.com.nlw.events.exceptions.SubscriptionConflictException;
import br.com.nlw.events.exceptions.UserIndicatorNotFoundException;
import br.com.nlw.events.model.Event;
import br.com.nlw.events.model.Subscription;
import br.com.nlw.events.model.User;
import br.com.nlw.events.repository.EventRepo;
import br.com.nlw.events.repository.SubscriptionRepo;
import br.com.nlw.events.repository.UserRepo;

@Service
public class SubscriptionService {
	
	@Autowired
	private EventRepo evtRepo;
	
	@Autowired
	private UserRepo userRepo;
	
	@Autowired
	private SubscriptionRepo subRepo;
	
	public SubscriptionResponse createNewSubscription(String eventName, User user, Integer userId) {
		
		Subscription subs = new Subscription();
		
		// Evento deve existir
		Event evt = evtRepo.findByPrettyName(eventName);
		if(evt==null) {
			throw new EventNotFoundException("Evento " + eventName + " não existe");
		}
		
		// Verificação de usuário
		User userRecuperado = userRepo.findByEmail(user.getEmail());
		if(userRecuperado==null) {
			userRecuperado = userRepo.save(user);			
		}
		
		// Verificação do indicador
		User indicador = null;
		if(userId!=null) {
			indicador = userRepo.findById(userId).orElse(null);
			if(indicador==null) {
				throw new UserIndicatorNotFoundException("Usuário indicador não existe - User: "+userId);
			}			
		}
		
		Subscription tmpSub = subRepo.findByEventAndSubscriber(evt, userRecuperado);
		if(tmpSub!=null) {
			throw new SubscriptionConflictException("Já existe inscrição para o usuário "+userRecuperado.getName()+" no evento "+eventName);
		}
		
		subs.setEvent(evt);
		subs.setSubscriber(userRecuperado);
		subs.setIndication(indicador);
		
		Subscription res = subRepo.save(subs);
		
		return new SubscriptionResponse(res.getSubscriptionNumber(), "http://codecraft.com/subscription/"+res.getEvent().getPrettyName()+"/"+res.getSubscriber().getId());
		
	}
	
	public List<SubscriptionRankingItem> getCompleteRanking(String preetyName){
		Event evt = evtRepo.findByPrettyName(preetyName);
		
		if(evt == null) {
			throw new EventNotFoundException("Evento não existe, logo não tem ranking");
		}
		
		return subRepo.generateRanking(evt.getEventId());
	}
	
	public SubscriptionRankingByUser getRankingByUser(String prettyName, Integer userId) {
		List<SubscriptionRankingItem> ranking = getCompleteRanking(prettyName);
		
		SubscriptionRankingItem item = ranking.stream().filter(i->i.user_id().equals(userId)).findFirst().orElse(null);
		
		if(item == null) {
			throw new UserIndicatorNotFoundException("Não há inscrições com indicação do usuário: "+userId);
		}
		
		Integer posicao = IntStream.range(0,ranking.size())
					.filter(pos->ranking.get(pos).user_id().equals(userId))
					.findFirst().getAsInt();
		
		return new SubscriptionRankingByUser(item,posicao+1);
	}
	
}
