(define (domain emergency_services_logistics_domain_durative_actions)

    (:requirements :strips :typing :fluents :adl :durative-actions)

    (:types
        location - object

        locatable - object   ; An entity that can be located

        box_place - object   ; A seat on the carrier that can bo occupied with a box.
                                ; The number of box_place identifies the total capacity of all the carriers

        agent - locatable    ; Robotic agent

        carrier - locatable

        person - locatable

        box - object

        content - object     ; Content of the box
    )

    (:predicates

        ; The position of a locatable entity
        (at ?x - locatable ?l - location)

        ; The content the person needs
        (needs_content ?p - person ?c - content)

        ; A content the person does not need anymore
        (is_satisfied ?p - person ?c - content)

        ; A content inside the box
        (is_inside_a_box ?c - content ?b - box)

        ; Empty box
        (is_empty ?b - box)

        ; A box loaded on the carrier
        (is_loaded_on_carrier ?b - box ?ca - carrier)

        ; A box which is not loaded on the carrier
        (is_not_loaded ?b - box)

        ; A box place associated to a carrier
        (box_place_belongs_to_carrier ?bp - box_place ?ca - carrier)

        ; A box place is occupied by a box or is free
        (is_occupied_by ?bp - box_place ?b - box)
        (is_free ?bp - box_place)

        ; A certain location is the depot
        (is_depot ?l - location)

        ; Predicate that tells if an agent does not perform any actions.
        ; It's like a semaphore to avoid the simultaneous execution of more actions.
        (is_idle ?a - agent)
    )

    (:functions
        (content_weight ?c - content)  ; Weight associated to a content
        (carrier_weight ?ca - carrier)  ; Weight associated to the carrier
    )

    ; Fills a box with a certain content and load it on the carrier, ready to be delivered
    (:durative-action fill_box_and_load_it_on_carrier
        :parameters (?a - agent ?ca - carrier ?b - box ?bp - box_place ?c - content ?l - location)
        
        ; The duration is equal to the sum of the duration of the filling and loading actions,
        ; which depend on the weight of the content
        :duration (= ?duration (* (content_weight ?c) 2))
        :condition (and 
            ; Semaphore
            (at start (is_idle ?a))
            
            ; The box must be empty
            (at start (is_empty ?b))
            
            ; The box must not be already loaded
            (at start (is_not_loaded ?b))
            
            ; The box_place passed as parameter must be free
            (at start (is_free ?bp))
            
            ; The position must be the depot. The filling and loading of a box can only
            ; take place in the depot.
            (over all (is_depot ?l))

            ; The box_place passed as a parameter must belong to the carrier
            (over all (box_place_belongs_to_carrier ?bp ?ca))
            
            ; The agent and the carrier must be at the depot
            (over all (at ?a ?l))
            (over all (at ?ca ?l))
        )
        :effect (and 
            ; Semaphore
            (at start (not (is_idle ?a)))
            
            (at start (not (is_empty ?b)))
            (at start (not (is_not_loaded ?b)))
            (at start (not (is_free ?bp)))
            
            ; The content is in the box, so it's not empty
            (at end (is_inside_a_box ?c ?b))
            
            ; The box is loaded on the carrier
            (at end (is_loaded_on_carrier ?b ?ca))
            
            ; The box_place of that carrier is now occupied by the box
            (at end (is_occupied_by ?bp ?b))
            
            ; Semaphore
            (at end (is_idle ?a))
            
            ; Increment the weight of the carrier
            (at end (increase (carrier_weight ?ca) (content_weight ?c)))
        )
    )

    ; Move an agent in a new location
    (:durative-action move_agent
        :parameters (?a - agent ?l1 - location ?l2 - location)
        
        ; The agent moves alone, without additional weight, so the duration is 1
        :duration (= ?duration 1)
        :condition (and 
            ; Semaphore
            (at start (is_idle ?a))

            ; The agent must be in the starting location
            (at start (at ?a ?l1))
        )
        :effect (and 
            ; Semaphore
            (at start (not (is_idle ?a)))

            ; The agent is in the arrival location
            (at start (not (at ?a ?l1)))
            (at end (at ?a ?l2))

            ; Semaphore
            (at end (is_idle ?a))
        )
    )
    
    ; Move an agent and a carrier into a new location
    (:durative-action move_agent_and_carrier
        :parameters (?a - agent ?ca - carrier ?l1 - location ?l2 - location)
        
        ; The agent moves with the carrier, so the duration is 1 plus the weight of the carrier
        :duration (= ?duration (+ (carrier_weight ?ca) 1))
        :condition (and 
            ; Semaphore
            (at start (is_idle ?a))

            ; The agent and the carrier must be in the starting location
            (at start (at ?a ?l1))
            (at start (at ?ca ?l1))
        )
        :effect (and 
            ; Semaphore
            (at start (not (is_idle ?a)))
            
            ; The agent and the carrier are in the arrival location
            (at start (not (at ?a ?l1)))
            (at start (not (at ?ca ?l1)))
            (at end (at ?a ?l2))
            (at end (at ?ca ?l2))
            
            ; Semaphore
            (at end (is_idle ?a))
        )
    )

    ; Unload a box from a carrier, deliver its content to a person and reload the empty box
    ; on the carrier
    (:durative-action unload_box_deliver_its_content_and_reload_it_on_carrier
        :parameters (?a - agent ?ca - carrier ?b - box ?c - content ?p - person ?l - location)
        
        ; Duration equal to the sum of the durations of the actions of: unloading (depending on
        ; the content weight), delivering (same duration) and reloading (duration 1 since the box
        ; is now empty)
        :duration (= ?duration (+ (* (content_weight ?c) 2) 1))
        :condition (and 
            ; Semaphore
            (at start (is_idle ?a))
            
            ; The box must contain that specific content
            (at start (is_inside_a_box ?c ?b))
            
            ; The box must be loaded on the carrier
            (at start (is_loaded_on_carrier ?b ?ca))
            
            ; The person must need that content
            (at start (needs_content ?p ?c))
            
            ; Agent, carrier and person must be at the depot
            (over all (at ?a ?l))
            (over all (at ?ca ?l))
            (over all (at ?p ?l))
        )
        :effect (and 
            ; Semaphore
            (at start (not (is_idle ?a)))
            
            ; The content is no longer inside the box
            (at start (not (is_inside_a_box ?c ?b)))
            
            ; The person does not need that content anymore
            (at start (not (needs_content ?p ?c)))

            (at end (is_empty ?b))
            (at end (is_satisfied ?p ?c))

            ; Semaphore
            (at end (is_idle ?a))
            
            ; Decrement the weight of the carrier
            (at end (decrease (carrier_weight ?ca) (content_weight ?c)))
        
            ; There are no effects
        )
    )
    
    ; Unload an empty box from the carrier
    (:durative-action unload_empty_box_from_carrier
        :parameters (?a - agent ?ca - carrier ?b - box ?bp - box_place ?l - location)
        
        ; The empty box has no weight, so the duration is 1
        :duration (= ?duration 1)
        :condition (and 
            ; Semaphore
            (at start (is_idle ?a))
            
            ; The box must be loaded on the carrier
            (at start (is_loaded_on_carrier ?b ?ca))
            
            ; The box_place must belong to that carrier and must be occupied by that box
            (at start (is_occupied_by ?bp ?b))
            (over all (box_place_belongs_to_carrier ?bp ?ca))
            
            ; The box must be empty
            (over all (is_empty ?b))
            
            ; The location must be the depot
            (over all (is_depot ?l))
            
            ; The agent and the carrier must be at the depot
            (over all (at ?a ?l))
            (over all (at ?ca ?l))
        )
        :effect (and 
            ; Semaphore
            (at start (not (is_idle ?a)))
            
            ; The box is not loaded on the carrier anymore
            (at start (not (is_loaded_on_carrier ?b ?ca)))
            
            ; The box_place is now free
            (at start (not (is_occupied_by ?bp ?b)))
            
            (at end (is_not_loaded ?b))
            (at end (is_free ?bp))
            
            ; Semaphore
            (at end (is_idle ?a))
        )
    )
    
)