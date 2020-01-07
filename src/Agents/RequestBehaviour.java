package Agents;
import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.CyclicBehaviour;
import jade.domain.DFService;
import jade.domain.FIPAException;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import java.util.ArrayList;
import java.util.List;

class RequestBehaviour extends CyclicBehaviour {
	private String conversationID;
	private AID requester;
	private String livre;
	private double prix;
	private int compteur;
	private List<AID> vendeurs = new ArrayList<>();
	private AID meilleureOffre;
	private double meilleurPrix;
	private int index;

	public RequestBehaviour(Agent agent, String livre, AID requester, String conversationID) {
		super(agent);
		this.livre = livre;
		this.requester = requester;
		this.conversationID = conversationID;
		System.out.println("Recherche des services...");
		vendeurs = chercherServices(myAgent, "book-selling");
		System.out.println("Liste des vendeurs trouvés :");
		try {
			for (AID aid : vendeurs) {
				System.out.println("====" + aid.getName());
			}
			++compteur;
			System.out.println("#########################################");
			System.out.println("Requête d'achat de livre:");
			System.out.println("From :" + requester.getName());
			System.out.println("Livre : " + livre);
			System.out.println("............................");
			System.out.println("Envoi de la requête....");
			ACLMessage msg = new ACLMessage(ACLMessage.CFP);
			msg.setContent(livre);
			msg.setConversationId(conversationID);
			msg.addUserDefinedParameter("compteur", String.valueOf(compteur));
			for (AID aid : vendeurs) {
				msg.addReceiver(aid);
			}
			System.out.println("....... En cours");
			Thread.sleep(5000);
			index = 0;
			myAgent.send(msg);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Override
	public void action() {
		try {
			MessageTemplate template = MessageTemplate.and(MessageTemplate.MatchConversationId(conversationID),
					MessageTemplate.or(MessageTemplate.MatchPerformative(ACLMessage.PROPOSE),
							MessageTemplate.MatchPerformative(ACLMessage.CONFIRM)));
			ACLMessage aclMessage = myAgent.receive(template);
			if (aclMessage != null) {
				switch (aclMessage.getPerformative()) {
				case ACLMessage.PROPOSE:
					prix = Double.parseDouble(aclMessage.getContent());
					System.out.println("***********************************");
					System.out.println("Conversation ID:" + aclMessage.getConversationId());
					System.out.println("Réception de l'offre :");
					System.out.println("From :" + aclMessage.getSender().getName());
					System.out.println("Prix=" + prix);
					if (index == 0) {
						meilleurPrix = prix;
						meilleureOffre = aclMessage.getSender();
					} else {
						if (prix < meilleurPrix) {
							meilleurPrix = prix;
							meilleureOffre = aclMessage.getSender();
						}
					}
					++index;
					if (index == vendeurs.size()) {
						index = 0;
						System.out.println("-----------------------------------");
						System.out.println("Conclusion de la transaction.......");
						ACLMessage aclMessage2 = new ACLMessage(ACLMessage.ACCEPT_PROPOSAL);
						aclMessage2.addReceiver(meilleureOffre);
						aclMessage2.setConversationId(conversationID);
						System.out.println("...... En cours");
						Thread.sleep(5000);
						myAgent.send(aclMessage2);
					}
					break;
				case ACLMessage.CONFIRM:
					System.out.println(".........................");
					System.out.println("Reçu de la confirmation ...");
					System.out.println("Conversation ID:" + aclMessage.getConversationId());
					ACLMessage msg3 = new ACLMessage(ACLMessage.INFORM);
					msg3.addReceiver(requester);
					msg3.setConversationId(conversationID);
					msg3.setContent("<transaction>" + "<livre>" + livre + "</livre>" + "<prix>" + meilleurPrix
							+ "</prix>" + "<fournisseur>" + aclMessage.getSender().getName() + "</fournisseur>"
							+ "</transaction");
					myAgent.send(msg3);
					break;
				}
			} else {
				block();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public List<AID> chercherServices(Agent agent, String type) {
		List<AID> vendeurs = new ArrayList<>();
		DFAgentDescription agentDescription = new DFAgentDescription();
		ServiceDescription serviceDescription = new ServiceDescription();
		serviceDescription.setType(type);
		agentDescription.addServices(serviceDescription);
		try {
			DFAgentDescription[] descriptions = DFService.search(agent, agentDescription);
			for (DFAgentDescription dfad : descriptions) {
				vendeurs.add(dfad.getName());
			}
		} catch (FIPAException e) {
			e.printStackTrace();
		}
		return vendeurs;
	}
}