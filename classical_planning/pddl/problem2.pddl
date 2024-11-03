(define (problem emergency_services_logistics_problem_2)

    (:domain emergency_services_logistics_domain)

    (:objects
        b1 b2 b3 - box
        l1 l2 l3 l4 l5 - location
        p1 p2 p3 p4 p5 p6 - person
        foodortools food medicine tools - content
        a1 a2 - agent
        c1 c2 - carrier
        bp1 bp2 bp3 bp4 - box_place
    )

    (:init

        ; Initially all the boxes, the carrier and the agent are at the depot
        (at b1 depot)
        (at b2 depot)
        (at b3 depot)
        (at a1 depot)
        (at a2 depot)
        (at c1 depot)
        (at c2 depot)
        
        ; Initially the people are in their respective locations
        (at p1 l1)
        (at p2 l1)
        (at p3 l2)
        (at p4 l3)
        (at p5 l4)
        (at p6 l5)

        ; Initially all the boxes are empty
        (isNotInsideABox food b1)
        (isNotInsideABox medicine b1)
        (isNotInsideABox tools b1)
        (isNotInsideABox foodortools b1)
        (isNotInsideABox food b2)
        (isNotInsideABox medicine b2)
        (isNotInsideABox tools b2)
        (isNotInsideABox foodortools b2)
        (isNotInsideABox food b3)
        (isNotInsideABox medicine b3)
        (isNotInsideABox tools b3)
        (isNotInsideABox foodortools b3)

        ; Initially the boxes are not loaded
        (isNotLoadedOnCarrier b1 c1)
        (isNotLoadedOnCarrier b2 c1)
        (isNotLoadedOnCarrier b3 c1)
        (isNotLoadedOnCarrier b1 c2)
        (isNotLoadedOnCarrier b2 c2)
        (isNotLoadedOnCarrier b3 c2)

        ; Initially the box_place are free
        (isFree bp1)
        (isFree bp2)
        (isFree bp3)
        (isFree bp4)

        ; Define the people's needs
        (needsContent p1 foodortools)
        (needsContent p2 medicine)
        (needsContent p3 medicine)
        (needsContent p4 medicine)
        (needsContent p4 food)
        (needsContent p5 food)
        (needsContent p5 medicine)
        (needsContent p5 tools)
        (needsContent p6 food)
        (needsContent p6 medicine)
        (needsContent p6 tools)
        
        ; Assign the box places to the carrier
        (boxPlaceBelongsToCarrier bp1 c1)
        (boxPlaceBelongsToCarrier bp2 c1)
        (boxPlaceBelongsToCarrier bp3 c2)
        (boxPlaceBelongsToCarrier bp4 c2)

    )

    (:goal (and
        (isSatisfied p1 foodortools)
        (isSatisfied p2 medicine)
        (isSatisfied p3 medicine)
        (isSatisfied p4 medicine)
        (isSatisfied p4 food)
        (isSatisfied p5 food)
        (isSatisfied p5 medicine)
        (isSatisfied p5 tools)
        (isSatisfied p6 food)
        (isSatisfied p6 medicine)
        (isSatisfied p6 tools)
    ))

)