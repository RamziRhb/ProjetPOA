package Agents;
import jade.core.Agent;
import jade.core.behaviours.CyclicBehaviour;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;

public class SellerBehaviour extends CyclicBehaviour {
	private String conversationID;

	public SellerBehaviour(Agent agent, String conversationID) {
		super(agent);
		this.conversationID = conversationID;
	}

	@Override
	public void action() {
		try {
			MessageTemplate messageTemplate = MessageTemplate.and(MessageTemplate.MatchConversationId(conversationID),
					MessageTemplate.MatchPerformative(ACLMessage.ACCEPT_PROPOSAL));
			ACLMessage aclMessage = myAgent.receive(messageTemplate);
			if (aclMessage != null) {
				System.out.println("--------------------------------");
				System.out.println("Conversation ID:" + aclMessage.getConversationId());
				System.out.println("Validation de la transaction .....");
				ACLMessage reply2 = aclMessage.createReply();
				reply2.setPerformative(ACLMessage.CONFIRM);
				System.out.println("...... En cours");
				Thread.sleep(5000);
				myAgent.send(reply2);
			} else {
				block();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}