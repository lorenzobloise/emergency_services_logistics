(define (problem emergency_services_logistics_problem_1_durative_actions)

    (:domain emergency_services_logistics_domain_durative_actions)

    (:objects
        b1 b2 b3 b4 b5 - box
        l1 l2 depot - location
        p1 p2 p3 - person
        food medicine - content
        a - agent
        ca - carrier
        bp1 bp2 bp3 bp4 - box_place
    )

    (:init

        ; Assign the following values:
        ; - Weight of the food: 2
        ; - Weight of the medicines: 1
        ; - Weight of the empty carrier: 1
        (= (content_weight food) 2)
        (= (content_weight medicine) 1)
        (= (carrier_weight ca) 1)

        (is_idle a)
        (at a depot)
        (at ca depot)
        (at p1 l1)
        (at p2 l1)
        (at p3 l2)
        
        (is_depot depot)

        (is_empty b1)
        (is_empty b2)
        (is_empty b3)
        (is_empty b4)
        (is_empty b5)

        (is_not_loaded b1)
        (is_not_loaded b2)
        (is_not_loaded b3)
        (is_not_loaded b4)
        (is_not_loaded b5)

        (needs_content p1 food)
        (needs_content p1 medicine)
        (needs_content p2 medicine)
        (needs_content p3 food)

        (is_free bp1)
        (is_free bp2)
        (is_free bp3)
        (is_free bp4)

        (box_place_belongs_to_carrier bp1 ca)
        (box_place_belongs_to_carrier bp2 ca)
        (box_place_belongs_to_carrier bp3 ca)
        (box_place_belongs_to_carrier bp4 ca)

    )

    (:goal (and
        (is_satisfied p1 food)
        (is_satisfied p1 medicine)
        (is_satisfied p2 medicine)
        (is_satisfied p3 food)
    ))

)
