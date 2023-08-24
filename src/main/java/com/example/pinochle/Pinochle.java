package com.example.pinochle;

import javafx.animation.*;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Group;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.util.Duration;

import java.net.URL;
import java.util.*;

/**
 * Main place where things happen
 */
public class Pinochle implements Initializable {

    /**
     * Pass/Bid
     */
    @FXML
    private GridPane bidActionsGrid;
    /**
     * Advances to next stage on click
     */
    @FXML
    private Button advanceButton;
    /**
     * Cards of opponent 1
     */
    @FXML
    private VBox opponent1VBox;
    /**
     * Cards of opponent 2
     */
    @FXML
    private VBox opponent2VBox;
    /**
     * Other team's score label
     */
    @FXML
    private Label otherTeamScoreLabel;
    /**
     * Player bid/pass label
     */
    @FXML
    private Label playerBidLabel;
    /**
     * Player cards container
     */
    @FXML
    private HBox playerCardsHBOX;
    /**
     * Player team's score label
     */
    @FXML
    private Label playerTeamScoreLabel;
    /**
     * Where scores are shown
     */
    @FXML
    private GridPane scoresGridPane;
    /**
     * Suits for picking trump
     */
    @FXML
    private GridPane suitsGrid;
    /**
     * Teammate cards
     */
    @FXML
    private HBox teammateHBox;
    /**
     * Starts game on click
     */
    @FXML
    private Button welcomeButton;
    /**
     * Just to look nice
     */
    @FXML
    private Group welcomeImageGroup;
    /**
     * Welcome!
     */
    @FXML
    private Label welcomeLabel;
    /**
     * Where meld is laid
     */
    @FXML
    private Group playersMeldsGroup;
    /**
     * Where bids are shown
     */
    @FXML
    private Group bidStackPanesGroup;
    /**
     * Shows trump suit
     */
    @FXML
    private StackPane trumpSuitIndicatorStackPane;
    /**
     * Shows dealer
     */
    @FXML
    private Group dealerChipsGroup;
    /**
     * Blocks user clicks
     */
    @FXML
    private Pane blockPane;


    /**
     * Amount of players
     */
    private int PLAYERCOUNT;
    /**
     * Image of back of card
     */
    private Image BACK;
    /**
     * Array of players
     */
    private Player[] players;
    /**
     * Images for each card in player's hand
     */
    private ImageView[] playerCardImages;
    /**
     * Deck of cards object
     */
    private Deck deck;
    /**
     * Player team's score
     */
    private int playerTeamScore;
    /**
     * Other team's score
     */
    private int otherTeamScore;
    /**
     * Trump suit of the hand
     */
    private String trumpSuit;
    /**
     * Dealer of the hand
     */
    private int dealer;
    /**
     * An array of actions players took in bidding phase
     */
    private int[] isBidding;
    /**
     * Current bid amount
     */
    private int bid;

    /**
     * Starts the program
     * @param url given
     * @param resourceBundle given
     */
    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        getVariables();
    }

    /**
     * Starts the game!
     */
    @FXML
    void startGame() {
        welcomeButton.setDisable(true);
        welcomeButton.setVisible(false);

        welcomeImageGroup.setDisable(true);
        welcomeImageGroup.setVisible(false);

        welcomeLabel.setDisable(true);
        welcomeLabel.setVisible(false);

        advanceButton.setDisable(false);
        advanceButton.setVisible(true);

        scoresGridPane.setDisable(false);
        scoresGridPane.setVisible(true);

        startHand();
    }

    /**
     * Initializes variables
     */
    private void getVariables() {
        dealer = 0;
        PLAYERCOUNT = 4;
        playerTeamScore = otherTeamScore = 0;
        BACK = new Image(Objects.requireNonNull(Card.class.getResourceAsStream("/images/back.png")));
        deck = new Deck();
        players = new Player[PLAYERCOUNT];
        for (int i = 0; i < PLAYERCOUNT; i++) {
            players[i] = new Player();
        }
    }

    /**
     * Deals the cards and sets images of them
     */
    private void deal() {
        clearHBox(playerCardsHBOX);
        clearVBox(opponent1VBox);
        clearHBox(teammateHBox);
        clearVBox(opponent2VBox);

        dealerChipsGroup.getChildren().get(dealer).setVisible(true);

        Card[][] deal = deck.deal();

        for (int i = 0; i < PLAYERCOUNT; i++) {
            players[i].setHand(deal[i]);
            System.out.println(players[i]);
        }

        setCards();
        for(int i=0; i<playerCardsHBOX.getChildren().size(); i++) {
            setImageViewUserData(i);
        }
        getPlayerCardImages();

        setSuitsGrid();
        setTrumpSuitIndicatorStackPane();
    }

    /**
     * Starts the hand by dealing and appropriately setting images
     */
    private void startHand() {
        for(int i=0; i<playersMeldsGroup.getChildren().size(); i++) {
            VBox parent = (VBox) playersMeldsGroup.getChildren().get(i);
            for (int j = 0; j< Objects.requireNonNull(parent).getChildren().size(); j++) {
                HBox subbox = (HBox) parent.getChildren().get(j);
                for(int k=0; k<subbox.getChildren().size(); k++) {
                    ((ImageView) subbox.getChildren().get(k)).setImage(null);
                }
            }
        }
        deal();
        setAdvanceButton(true);
        setBidStackPanes(false);
        ((ImageView) trumpSuitIndicatorStackPane.getChildren().get(0)).setImage(null);
        advanceButton.setOnAction(this::startBid);
        advanceButton.setText("To Bid");
        bid = 52;
    }

    /**
     * Starts a new hand
     * @param event button click
     */
    @FXML
    void startHand(ActionEvent event) {
        dealer = (dealer+1)%4;
        dealerChipsGroup.getChildren().get((dealer+3)%4).setVisible(false);
        dealerChipsGroup.getChildren().get(dealer).setVisible(true);
        startHand();
    }

    /**
     * Starts bid phase
     * @param event button click
     */
    @FXML
    void startBid(ActionEvent event) {
        setBidActionsGrid();
        setBidStackPanes(true);
        advanceButton.setOnAction(this::startHand);
        advanceButton.setText("New Deal");
        setAdvanceButton(false);
        setBidLabels();
        isBidding = new int[] {0, 0, 0, 0};
        bid(false, false, false);
    }

    /**
     * Gets an ArrayList of images to easily access them
     */
    private void getPlayerCardImages() {
        playerCardImages = new ImageView[20];
        for(int i = 0; i< playerCardsHBOX.getChildren().size(); i++) {
            playerCardImages[i] = (ImageView) playerCardsHBOX.getChildren().get(i);
        }
    }

    /**
     * Sets data to an ImageView so that it can be accessed when clicked
     * @param i data to input
     */
    private void setImageViewUserData(int i) {
        ImageView imageView = (ImageView) playerCardsHBOX.getChildren().get(i);
        imageView.setUserData(i);

        imageView.setOnMouseClicked(event -> playCard((int) imageView.getUserData()));
    }

    /**
     * ON CLICKING A CARD: if card is legal to play, play the card and remove it from the hand and sort the hand.
     * TODO: all of that
     * @param i card clicked
     */
    private void playCard(int i) {
        players[0].playCard(i);

        playerCardImages[i].setImage(null);

        //TranslateTransition transition = new TranslateTransition(Duration.seconds(1), toMove);
        //transition.setByX(-17*(i-10));
        //transition.setByY(-100);
        //transition.play();
        //transition.setOnFinished(e -> );

        toCenter();
    }

    /**
     * Centers cards after clicking one
     */
    private void toCenter() {
        int nullCounter = 0;
        ArrayList<Image> notNull = new ArrayList<>();
        int size = playerCardImages.length;
        for (ImageView playerCardImage : playerCardImages) {
            if (playerCardImage.getImage() != null)
                notNull.add(playerCardImage.getImage());
            else nullCounter++;
        }
        int index = 0;
        for(int i=0; i<size; i++) {
            if(i<nullCounter/2 || i>=20-Math.round((double) nullCounter/2))
                playerCardImages[i].setImage(null);
            else {
                playerCardImages[i].setImage(notNull.get(index));
                setImageViewUserData(index);
                index++;
            }
        }
    }

    /**
     * Clears all children from Vbox
     * @param parent VBox to clear
     */
    private void clearVBox(VBox parent) {
        parent.getChildren().clear();
    }

    /**
     * Clears all children from HBox
     * @param parent HBox to clear
     */
    private void clearHBox(HBox parent) {
        parent.getChildren().clear();
    }

    /**
     * Takes empty Hbox and Vbox and fills them with cards according to who they are
     */
    private void setCards() {
        for(int i=0; i<20; i++) {
            playerCardsHBOX.getChildren().add(buildChild(players[0].getHand()[i].getImage(), 57, 79, -40*(i+1.5), 0, 0));
            opponent1VBox.getChildren().add(buildChild(BACK, 30, 49, 0, -42*(i), 90));
            teammateHBox.getChildren().add(buildChild(BACK, 30, 49, -23*(i-2), 0, 0));
            opponent2VBox.getChildren().add(buildChild(BACK, 30, 49, 0, -42*(i), 90));
        }
    }

    /**
     * Takes a bunch of parameters and builds a child for a HBox or Vbox
     * @param image image to put in child
     * @param x width
     * @param y height
     * @param dx translation in x direction
     * @param dy translation in y direction
     * @param rot rotation angle in degrees
     * @return result of the child
     */
    private ImageView buildChild(Image image, double x, double y, double dx, double dy, double rot) {
        ImageView imageView = new ImageView(image);
        imageView.setFitWidth(x);
        imageView.setFitHeight(y);
        imageView.setTranslateX(dx);
        imageView.setTranslateY(dy);
        imageView.setRotate(rot);

        return imageView;
    }


    /**
     * Sets the grid used to pick trump suit
     */
    private void setSuitsGrid() {
        for(int i=0; i<suitsGrid.getChildren().size(); i++) {
            ImageView imageView = (ImageView) suitsGrid.getChildren().get(i);
            imageView.setUserData(i);
            imageView.setOnMouseClicked(event -> pickSuit((int) imageView.getUserData()));
        }
        suitsGrid.setVisible(false);
        suitsGrid.setDisable(true);
    }


    /**
     * On click, determine which suit is trump for this hand
     * @param i clicked, which to use as index for suits
     */
    private void pickSuit(int i) {
        String[] suits = new String[] {"S", "C", "H", "D"};
        trumpSuit = suits[i];
        suitsGrid.setVisible(false);
        setAdvanceButton(true);

        layMeld();
        ((ImageView) trumpSuitIndicatorStackPane.getChildren().get(0)).setImage(new Image(Objects.requireNonNull(Card.class.getResourceAsStream("/images/" + trumpSuit + ".png"))));
    }

    /**
     * Lays all players' meld on the table
     */
    private void layMeld() {
        for(int i=0; i<playersMeldsGroup.getChildren().size(); i++) {
            VBox parent = (VBox) playersMeldsGroup.getChildren().get(i);
            ArrayList<String> meldCards = getMeldCards(players[i].getMeldCardsHashMap(trumpSuit));
            for (int j = 0; j < Objects.requireNonNull(parent).getChildren().size(); j++) {
                HBox subBox = (HBox) parent.getChildren().get(j);
                clearHBox(subBox);
                for (int k = 0; k < 5; k++) {
                    if (5 * j + k < meldCards.size()) {
                        Image image = new Image(Objects.requireNonNull(Card.class.getResourceAsStream("/images/" + meldCards.get(5 * j + k) + ".png")));
                        subBox.getChildren().add(buildChild(image, 45, 70, -35 * k, -50 * j, 0));
                    }
                }
            }
        }
    }

    /**
     * Bidding phase
     * @param startComputersAtStart whether to start the cpu bids from the player's left
     * @param playerTurn whether it is the player's turn
     * @param playerHasBid whether the player bid
     */
    private void bid(boolean startComputersAtStart, boolean playerTurn, boolean playerHasBid) {

        int bidder = 1;
        if(!startComputersAtStart)
            bidder += dealer;


        // Human player
        if(playerTurn) {
            if(playerHasBid) {
                playerBidLabel.setText("Bid: "+bid);
                isBidding[0] = 1;
                if(bid!=50) {
                    bid += 2;
                    if (bid > 60)
                        bid += 3;
                }
            } else {
                isBidding[0] = -1;
                playerBidLabel.setText("Pass!");
                if(biddersLeft()==0) {
                    setAdvanceButton(true);
                    playerTeamScore-=50;
                    playerTeamScoreLabel.setText(String.valueOf(playerTeamScore));
                }
            }
        }

        int biddersLeft = 0;
        Timeline time = new Timeline();
        for (int i = bidder; i < PLAYERCOUNT; i++) {
            int finalI = i;
            if(isBidding[i]!=-1) {
                biddersLeft++;
                time.getKeyFrames().add(new KeyFrame(Duration.seconds(biddersLeft), e -> computerBid(finalI)));
            }
        }
        time.getKeyFrames().add(new KeyFrame(Duration.seconds(1), (KeyValue) null));
        time.playFromStart();
        if(isBidding[0]!=-1 || biddersLeft()<=1)
            time.setOnFinished(e -> editBidActionsGrid());
        else time.setOnFinished(e -> bid(true, false, false));

    }

    /**
     * Counts the amount of bidders still in the bid
     * @return amount left
     */
    private int biddersLeft() {
        int bidders = 0;
        for(int bit : isBidding) {
            if(bit!=-1) bidders++;
        }
        return bidders;
    }

    /**
     * Gets the cards needed to lay meld
     * @param meldCardsHashMap Cards and amounts needed
     * @return Cards in order to lay
     */
    private ArrayList<String> getMeldCards(HashMap<String, Integer> meldCardsHashMap) {
        ArrayList<String> meldCards = new ArrayList<>();
        for(String card : meldCardsHashMap.keySet()) {
            for(int i=0; i<meldCardsHashMap.get(card); i++) {
                meldCards.add(card);
            }
        }
        return meldCards;
    }

    /**
     * Sets the bid labels for bid phase
     */
    private void setBidLabels() {
        String[] names = new String[] {"Player", "Opponent 1", "Teammate", "Opponent 2"};
        for(int i=0; i<bidStackPanesGroup.getChildren().size(); i++) {
            ((Label) ((StackPane) bidStackPanesGroup.getChildren().get(i)).getChildren().get(1)).setText(names[i]);
        }
    }

    /**
     * Sets bid actions grid for when it is player's turn to bid
     */
    private void setBidActionsGrid() {
        for(int i=0; i<bidActionsGrid.getChildren().size(); i++) {
            Button button = (Button) bidActionsGrid.getChildren().get(i);
            button.setUserData(i);
            button.setOnAction(event -> bidAction((int) button.getUserData()));
        }
        ((Button) bidActionsGrid.getChildren().get(0)).setText("Pass");
    }

    /**
     * Reads the user's decision to bid or pass
     * @param i decision
     */
    private void bidAction(int i) {
        bidActionsGrid.setVisible(false);
        bidActionsGrid.setDisable(true);
        bid(true, true, i==1);
    }

    /**
     * Sets the display of bids
     * @param on show/hide
     */
    private void setBidStackPanes(boolean on) {
        for(int i=0; i<bidStackPanesGroup.getChildren().size(); i++) {
            StackPane stackPane = (StackPane) bidStackPanesGroup.getChildren().get(i);
            stackPane.setDisable(!on);
            stackPane.setVisible(on);
        }
    }

    /**
     * What the computer will do on their bid
     * @param bidder computer whose turn it is
     */
    private void computerBid(int bidder) {
        Label label = (Label) ((StackPane) bidStackPanesGroup.getChildren().get(bidder)).getChildren().get(1);
        int max = max(getExpectedPoints(bidder));
        boolean hasBid = bid<=max;

        if(isBidding[bidder]==0 && biddersLeft()==1) bid = 50; // Bid dropped on computer

        if(isBidding[bidder]!=-1 && hasBid && biddersLeft()>1) { // Computer bidding
            isBidding[bidder] = 1;
            assert label != null;
            label.setText("Bid: " + bid);
            bid += 2;
            if (bid > 60) bid += 3;
        } else if(isBidding[bidder]==1 && biddersLeft()==1) { // Computer wins bid
            computerWonBid(bidder);
        } else if(isBidding[bidder]==0 && !hasBid && biddersLeft()==1) { // Computer gets dropped on but passes
            int team = bidder%2;
            int score;
            Label scoreLabel = playerTeamScoreLabel;
            if(team==1) {
                otherTeamScore-=bid;
                score = otherTeamScore;
                scoreLabel = otherTeamScoreLabel;
            } else {
                playerTeamScore-=bid;
                score = playerTeamScore;
            }
            setAdvanceButton(true);
            scoreLabel.setText(String.valueOf(score));
            isBidding[bidder] = -1;
            assert label != null;
            label.setText("Pass!");
        } else { // Computer passes
            isBidding[bidder] = -1;
            assert label != null;
            label.setText("Pass!");
        }
    }

    /**
     * Edits bid actions grid for player's turn
     */
    private void editBidActionsGrid() {
        if(isBidding[0]!=-1 && biddersLeft()>1) { // Player is in bid, but so are others
            bidActionsGrid.setDisable(false);
            ((Button) bidActionsGrid.getChildren().get(1)).setText("Bid "+bid);
            bidActionsGrid.setVisible(true);
        } else if(isBidding[0]==0 && biddersLeft()==1) { // Bid is dropped on player
            bid = 50;
            bidActionsGrid.setDisable(false);
            ((Button) bidActionsGrid.getChildren().get(0)).setText("Throw");
            ((Button) bidActionsGrid.getChildren().get(1)).setText("Bid "+bid);
            bidActionsGrid.setVisible(true);
        } else if(isBidding[0]==1 && biddersLeft()==1) { // Player has won bid
            suitsGrid.setVisible(true);
            suitsGrid.setDisable(false);
            suitsGrid.toFront();
            setBidStackPanes(false);
        }
    }

    /**
     * What the computer does when they win the bid
     * @param bidder computer that won bid
     */
    private void computerWonBid(int bidder) {
        String[] suits = new String[] {"C", "H", "S", "D"};
        int[] expectedPoints = getExpectedPoints(bidder);
        int max = max(expectedPoints);
        for(int i=0; i<PLAYERCOUNT; i++) {
            if(expectedPoints[i] == max) trumpSuit = suits[i];
        }
        setAdvanceButton(true);
        setBidStackPanes(false);
        layMeld();
        ((ImageView) trumpSuitIndicatorStackPane.getChildren().get(0)).setImage(new Image(Objects.requireNonNull(Card.class.getResourceAsStream("/images/" + trumpSuit + ".png"))));
    }

    /**
     * Sets the trump suit indicator
     */
    private void setTrumpSuitIndicatorStackPane() {
        ImageView imageView = new ImageView();
        imageView.setFitWidth(48);
        imageView.setFitHeight(48);
        trumpSuitIndicatorStackPane.getChildren().add(imageView);
    }

    /**
     * Sets the advance button
     * @param on show/hide
     */
    private void setAdvanceButton(boolean on) {
        advanceButton.setDisable(!on);
        advanceButton.setVisible(on);
    }

    /**
     * Gets the expected points to pull in per suit
     * @param player player whose hand to calculate
     * @return points per suit
     */
    private int[] getExpectedPoints(int player) {
        int[] expectedPointsArray = new int[PLAYERCOUNT];
        Card[] hand = players[player].getHand();
        String[] suits = new String[] {"C", "H", "S", "D"};
        int[] cardsInEachSuit = new int[suits.length];
        for (Card card : hand) {
            for (int i = 0; i < suits.length; i++) {
                if (card.getSuit().equals(suits[i])) cardsInEachSuit[i]++;
            }
        }

        for(int suit=0; suit<suits.length; suit++) {
            double expectedPoints = players[player].calcMeld()[suit] + 8;

            // Adding uncontested trumps
            int pointsInTrump = 0;
            int kingsInTrump = 0;
            int tensInTrump = 0;
            int acesInTrump = 0;
            for (Card card : hand) {
                if (card.getSuit().equals(suits[suit])) {
                    switch (card.getValue()) {
                        case "K" -> kingsInTrump++;
                        case "T" -> tensInTrump++;
                        case "A" -> acesInTrump++;
                    }
                }
            }
            pointsInTrump += kingsInTrump;
            if (tensInTrump > 0) pointsInTrump += tensInTrump - 1;
            int pointsInTrumpUsed = 0;
            int cardsLeftInTrump = cardsInEachSuit[suit];
            for (int i = 0; i < suits.length; i++) {
                if (i != suit) {
                    ArrayList<int[]> combos = getnCRrCombos(3, 20-cardsInEachSuit[i]);
                    double totalPermutations = 0;
                    for(int[] combo : combos) {
                        totalPermutations+=getPermutations(combo);
                    }
                    double expectedUncontestedTrump = 0;
                    for(int[] combo : combos) {
                        if(min(combo) > cardsInEachSuit[i]) {
                            expectedUncontestedTrump += getPermutations(combo) * (min(combo) - cardsInEachSuit[i]) / totalPermutations;
                        }
                    }

                    if (expectedUncontestedTrump > 0 && pointsInTrump > 0) {
                        expectedPoints++;
                        pointsInTrump--;
                        pointsInTrumpUsed++;
                    }
                    if(expectedUncontestedTrump>0) {
                        expectedPoints += 7.0 / 3 * (Math.min(cardsInEachSuit[suit], expectedUncontestedTrump));
                        cardsLeftInTrump -= (int) Math.min(cardsInEachSuit[suit], expectedUncontestedTrump);
                    }
                }
            }

            // Adding aces going through
            for (int i = 0; i < suits.length; i++) {
                if (i != suit) {
                    int aces = 0;
                    for (Card card : hand) {
                        if (card.getSuit().equals(suits[i]) && card.getValue().equals("A")) aces++;
                    }
                    ArrayList<int[]> combos = getnCRrCombos(3, 20-cardsInEachSuit[i]);
                    double totalPermutations = 0;
                    for(int[] combo : combos) {
                        totalPermutations+=getPermutations(combo);
                    }
                    double expectedAcesTaken = 0;
                    for(int[] combo : combos) {
                        expectedAcesTaken += getPermutations(combo) * Math.min(aces, min(combo)) / totalPermutations;
                    }

                    expectedPoints += 7 * expectedAcesTaken / 3;
                }
            }

            // Adding remaining high trump cards
            expectedPoints += 7.0 / 3 * Math.min(acesInTrump, cardsLeftInTrump);

            if (pointsInTrumpUsed <= kingsInTrump) expectedPoints += tensInTrump;
            else expectedPoints += tensInTrump + kingsInTrump - pointsInTrumpUsed;

            // Adding remaining trump cards for if player can run everyone else out
            ArrayList<int[]> combos = getnCRrCombos(3, 20-cardsInEachSuit[suit]);
            double totalPermutations = 0;
            for(int[] combo : combos) {
                totalPermutations+=getPermutations(combo);
            }
            double trumpsLeft = 0;
            for(int[] combo : combos) {
                if(max(combo) < cardsInEachSuit[suit]) {
                    trumpsLeft += getPermutations(combo) * (cardsInEachSuit[suit] - max(combo)) / totalPermutations;
                }
            }

            expectedPoints += 8.0/3 * trumpsLeft;

            // Adding 2 for last trick
            if ((acesInTrump >= 2 && cardsInEachSuit[suit] >= 4) || (acesInTrump >= 1 && cardsInEachSuit[suit] >= 6) || trumpsLeft > 0) expectedPoints += 2;

            expectedPointsArray[suit] = (int) Math.round(expectedPoints);
        }
        return expectedPointsArray;
    }

    /**
     * Gets the max of an array
     * @param nums array to get max
     * @return max number
     */
    private int max(int[] nums) {
        int max = nums[0];
        for(int i : nums) {
            max = Math.max(max, i);
        }
        return max;
    }

    /**
     * Gets the min of an array
     * @param nums array to get min
     * @return min number
     */
    private int min(int[] nums) {
        int min = nums[0];
        for(int i : nums) {
            min = Math.min(min, i);
        }
        return min;
    }

    /**
     * Gets all combinations of nCRr (combination with replacement)
     * @param n n
     * @param r r
     * @return all combinations
     */
    private ArrayList<int[]> getnCRrCombos(int n, int r) {
        ArrayList<int[]> nCRrCombos = new ArrayList<>();
        if(r==0) {
            int[] combo = new int[n];
            nCRrCombos.add(combo);
        } else if(n==1) {
            int[] combo = new int[n];
            combo[0] = r;
            nCRrCombos.add(combo);
        } else {
            for(int i=0; i<=r; i++) {
                for(int[] append : getnCRrCombos(n-1, i)) {
                    int[] combo = new int[n];
                    combo[n - 1] = r-i;
                    System.arraycopy(append, 0, combo, 0, append.length);
                    nCRrCombos.add(combo);
                }
            }
        }
        return nCRrCombos;
    }

    /**
     * Calculates n!
     * @param n n
     * @return n!
     */
    private long factorial(int n) {
        if(n<=1)
            return 1;
        return n*factorial(n-1);
    }

    /**
     * Calculates amount of permutations for each combination of numbers
     * @param combo combination to count perms
     * @return amount of perms
     */
    private double getPermutations(int[] combo) {
        double permutations=1;
        int add = 0;
        for(int i : combo) {
            add+=i;
            permutations/=factorial(i);
        }
        permutations*=factorial(add);
        return permutations;
    }
}