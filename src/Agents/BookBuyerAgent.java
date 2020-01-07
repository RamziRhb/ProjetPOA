package Agents;
import jade.core.Agent;
import jade.core.AID;
import jade.core.behaviours.CyclicBehaviour;
import jade.core.behaviours.ParallelBehaviour;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;

public class BookBuyerAgent extends Agent {
	ParallelBehaviour parallelBehaviour;
	int requesterCount;

	@Override
	protected void setup() {
		System.out.println("Salut je suis l'agent Acheteur mon nom est:" + this.getAID().getName());
		parallelBehaviour = new ParallelBehaviour();
		addBehaviour(parallelBehaviour);
		parallelBehaviour.addSubBehaviour(new CyclicBehaviour() {
			@Override
			public void action() {
				MessageTemplate template = MessageTemplate.MatchPerformative(ACLMessage.REQUEST);
				ACLMessage aclMessage = receive(template);
				if (aclMessage != null) {
					String livre = aclMessage.getContent();
					AID requester = aclMessage.getSender();
					++requesterCount;
					String conversationID = "transaction_" + livre + "_" + requesterCount;
					parallelBehaviour.addSubBehaviour(new RequestBehaviour(myAgent, livre, requester, conversationID));
				} else
					block();
			}
		});
	}

	@Override
	protected void beforeMove() {
		System.out.println("Avant de migrer vers une nouvelle location.....");
	}

	@Override
	protected void afterMove() {
		System.out.println("Je viens d'arriver à une nouvelle location.....");
	}

	@Override
	protected void takeDown() {
		System.out.println("avant de mourir .....");
	}
}