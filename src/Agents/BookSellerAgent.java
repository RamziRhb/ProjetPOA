package Agents;

import java.util.HashMap;
import java.util.Map;

import jade.core.*;
import jade.core.behaviours.*;
import jade.domain.*;
import jade.domain.FIPAAgentManagement.*;
import jade.lang.acl.*;

public class BookSellerAgent extends Agent {
	private Map<String, Double> data = new HashMap<>();
	private ParallelBehaviour parallelBehaviour;

	@Override
	protected void setup() {
		data.put("XML", new Double(230 + Math.random() * 200));
		data.put("JAVA", new Double(460 + Math.random() * 200));
		data.put("IOT", new Double(540 + Math.random() * 200));
		System.out.println("....... Vendeur " + this.getAID().getName());
		System.out.println("--------------");

		System.out.println("Publication du service dans Directory Facilitator...");
		DFAgentDescription agentDescription = new DFAgentDescription();
		agentDescription.setName(this.getAID());
		ServiceDescription serviceDescription = new ServiceDescription();
		serviceDescription.setType("book-selling");
		serviceDescription.setName("book-trading");
		agentDescription.addServices(serviceDescription);
		try {
			DFService.register(this, agentDescription);
		} catch (FIPAException e1) {
			e1.printStackTrace();
		}

		parallelBehaviour = new ParallelBehaviour();
		addBehaviour(parallelBehaviour);
		parallelBehaviour.addSubBehaviour(new CyclicBehaviour() {
			@Override
			public void action() {
				try {
					MessageTemplate messageTemplate = MessageTemplate.MatchPerformative(ACLMessage.CFP);
					ACLMessage aclMessage = receive(messageTemplate);
					if (aclMessage != null) {
						System.out.println("Conversation ID:" + aclMessage.getConversationId());
						String livre = aclMessage.getContent();
						Double prix = data.get(livre);
						ACLMessage reply = aclMessage.createReply();
						reply.setPerformative(ACLMessage.PROPOSE);
						reply.setContent(prix.toString());
						System.out.println("...... En cours");
						Thread.sleep(5000);
						send(reply);

						parallelBehaviour.addSubBehaviour(new SellerBehaviour(myAgent, aclMessage.getConversationId()));
					} else {
						block();
					}
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}

	@Override
	protected void takeDown() {
		try {
			DFService.deregister(this);
		} catch (FIPAException e) {
			e.printStackTrace();
		}
	}
}