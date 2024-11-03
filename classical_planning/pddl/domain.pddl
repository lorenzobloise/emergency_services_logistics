(define (domain emergency_services_logistics_domain)

    (:requirements :strips :typing :equality :quantified-preconditions :conditional-effects)

    (:types
        location - object

        locatable - object   ; An entity that can be located

        box_place - object   ; A seat on the carrier that can bo occupied with a box.
                                ; The number of box_place identifies the total capacity of all the carriers

        agent - locatable    ; Robotic agent

        carrier - locatable

        person - locatable

        box - locatable

        content - object     ; Content of the box
    )

    (:constants
        depot - location     ;The depot is the only object that can be considered as a constant,
                                ; and is modeled as a specific location
    )

    (:predicates
        ; The position of a locatable entity
        (at ?x - locatable ?l - location)

        ; The content the person needs
        (needsContent ?p - person ?c - content)

        ; A content the person does not need anymore
        (isSatisfied ?p - person ?c - content)

        ; A content inside the box
        (isInsideABox ?c - content ?b - box)

        ; A content which is not inside the box
        (isNotInsideABox ?c - content ?b - box)

        ; A box loaded on the carrier
        (isLoadedOnCarrier ?b - box ?c - carrier)

        ; A box which is not loaded on the carrier
        (isNotLoadedOnCarrier ?b - box ?c - carrier)

        ; A box place associated to a carrier
        (boxPlaceBelongsToCarrier ?bp - box_place ?c - carrier)

        ; A box place is occupied by a box or is free
        (isOccupiedBy ?bp - box_place ?b - box)
        (isFree ?bp - box_place)
    )


    ; Move the agent and the carrier to a new location
    (:action move_Agent_And_Carrier
        :parameters (?a - agent ?c - carrier ?l1 - location ?l2 - location)
        :precondition (and
            (not (= ?l1 ?l2))  ; The two locations must be different
            (at ?a ?l1)        ; Both the agent and the carrier must be in the same starting location
            (at ?c ?l1)
        )
        :effect (and
            ; The agent and the carrier move
            (at ?c ?l2)
            (not (at ?c ?l1))
            (at ?a ?l2)
            (not (at ?a ?l1))

            ; All the boxes loaded on the carrier change their location
            (forall (?b - box)
                (when (isLoadedOnCarrier ?b ?c) (and (at ?b ?l2) (not (at ?b ?l1))))
            )
        )
    )

    ; Move the agent to a new location
    (:action move_agent
        :parameters (?a - agent ?l1 - location ?l2 - location)
        :precondition (and
            (not (= ?l1 ?l2)) ; The two locations must be different
            (at ?a ?l1)
        )
        :effect (and
            (at ?a ?l2)
            (not (at ?a ?l1))
        )
    )

    ; Fill a box with the content and loads it on the carrier
    (:action fill_Box_And_Load_It_On_Carrier
        :parameters (?a - agent ?ca - carrier ?b - box ?bp - box_place ?c - content)
        :precondition (and 
            ; The agent, the carrier and the box must be at the depot
            (at ?a depot)
            (at ?ca depot)
            (at ?b depot)

            ; The box must be empty
            (forall (?c1 - content) 
                (isNotInsideABox ?c1 ?b)
            )

            ; The box must not be already loaded on the carrier
            (forall (?c1 - carrier)
                (isNotLoadedOnCarrier ?b ?c1)
            )

            ; The box_place must belong to the carrier and must not be occupied
            (boxPlaceBelongsToCarrier ?bp ?ca)
            (isFree ?bp)
        )
        :effect (and 
            ; The content is put inside the box, so the box is not empty
            (isInsideABox ?c ?b)
            (not (isNotInsideABox ?c ?b))

            ; The box is loaded on the carrier
            (isLoadedOnCarrier ?b ?ca)
            (not (isNotLoadedOnCarrier ?b ?ca))

            ; The box_place becomes occupied by the box and is not free anymore
            (isOccupiedBy ?bp ?b)
            (not (isFree ?bp))
        )
    )

    ; Unload the box from the carrier, empty it, deliver its content to the person and reload it
    ; on the carrier
    (:action unload_Box_Deliver_Its_Content_And_Reload_It_On_Carrier
        :parameters (?a - agent ?ca - carrier ?b - box ?c - content ?p - person ?l - location)
        :precondition (and 
            ; The agent, the carrier, the box and the person must be in the same location
            (at ?a ?l)
            (at ?ca ?l)
            (at ?b ?l)
            (at ?p ?l)
            
            ; The box must contain the content
            (isInsideABox ?c ?b)

            ; The box must be loaded on the carrier
            (isLoadedOnCarrier ?b ?ca)

            ; The person has to need that content
            (needsContent ?p ?c)
        )
        :effect (and 
            ; The person does not need that content anymore
            (not (needsContent ?p ?c))
            (isSatisfied ?p ?c)

            ; The content is not inside any box
            (not (isInsideABox ?c ?b))
            (isNotInsideABox ?c ?b)

            ; There's no need to add any effect
        )
    )

    ; Unload an empty box from the carrier
    (:action unload_Empty_Box_From_Carrier
        :parameters (?a - agent ?c - carrier ?b - box ?bp - box_place)
        :precondition (and 
            ; The agent, the carrier and the box must be at the depot
            (at ?c depot)
            (at ?a depot)
            (at ?b depot)

            ; The box must be located on the carrier
            (isLoadedOnCarrier ?b ?c)
            
            ; The box must be empty
            (forall (?co - content)
                (isNotInsideABox ?co ?b)
            )

            ; The box_place must belong to that carrier and must be occupied by that box
            (isOccupiedBy ?bp ?b)
            (boxPlaceBelongsToCarrier ?bp ?c)
        )
        :effect (and 
            ; The box is no longer loaded on the carrier
            (not (isLoadedOnCarrier ?b ?c))
            (isNotLoadedOnCarrier ?b ?c)

            ; The box_place is now free
            (not (isOccupiedBy ?bp ?b))
            (isFree ?bp)
        )
    )

)