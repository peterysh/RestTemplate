This project exists for trading point in application.
In specific, if a user charges points using real money, we have to very care the points.

I think @Version is useful for this system. Without @Lock.
Cause in this toy project, Member table has @Version for points field.
If just for confirm my points, lock the entity is quite excessive cost.
Also, every time do something with Member entity, we need to care about lock exception.

Yes, may you recognized, just make Point entity and make a relation @OneToOne between Member and Point entities.
But, it's just a toy project, so I did like this. If you use this codes in your application, U need to consist
some options.

Thank U!