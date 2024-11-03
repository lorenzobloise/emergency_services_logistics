(define (problem emergency_services_logistics_problem_1)

    (:domain emergency_services_logistics_domain)

    (:objects
        b1 b2 b3 b4 b5 - box
        l1 l2 - location
        p1 p2 p3 - person
        food medicine - content
        a - agent
        c - carrier
        bp1 bp2 bp3 bp4 - box_place
    )

    (:init

		; Initially all the boxes, the carrier and the agent are at the depot
        (at b1 depot)
        (at b2 depot)
        (at b3 depot)
        (at b4 depot)
        (at b5 depot)
        (at a depot)
        (at c depot)
        
        ; Initially the people are in their respective locations
        (at p1 l1)
        (at p2 l1)
        (at p3 l2)

		; Initially all the boxes are empty
        (isNotInsideABox food b1)
        (isNotInsideABox medicine b1)
        (isNotInsideABox food b2)
        (isNotInsideABox medicine b2)
        (isNotInsideABox food b3)
        (isNotInsideABox medicine b3)
        (isNotInsideABox food b4)
        (isNotInsideABox medicine b4)
        (isNotInsideABox food b5)
        (isNotInsideABox medicine b5)

		; Initially the boxes are not loaded
        (isNotLoadedOnCarrier b1 c)
        (isNotLoadedOnCarrier b2 c)
        (isNotLoadedOnCarrier b3 c)
        (isNotLoadedOnCarrier b4 c)
        (isNotLoadedOnCarrier b5 c)

		; Initially the box_place are free
        (isFree bp1)
        (isFree bp2)
        (isFree bp3)
        (isFree bp4)

		; Define the people's needs
        (needsContent p1 food)
        (needsContent p1 medicine)
        (needsContent p2 medicine)
        (needsContent p3 food)

		; Assign the box places to the carrier
        (boxPlaceBelongsToCarrier bp1 c)
        (boxPlaceBelongsToCarrier bp2 c)
        (boxPlaceBelongsToCarrier bp3 c)
        (boxPlaceBelongsToCarrier bp4 c)

    )

    (:goal (and
        (isSatisfied p1 food)
        (isSatisfied p1 medicine)
        (isSatisfied p2 medicine)
        (isSatisfied p3 food)
    ))

)