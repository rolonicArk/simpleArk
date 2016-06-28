# simpleArk
A top-down implementation of a Rolonic Arc

## An Ark

An ark is a collection of rolons, each with a unique UUID which does not change over time.
A UUID then establishes the identity of a rolon.

The ark is updated by processing a series of transactions,
where each transaction creates or modifies one or more rolons.

## A Rolon

A rolon is a sorted map of values, ordered by time.
Each rolon value is keyed by a type-1 UUID (timestamp), which is
also the UUID of the transaction that created the value and the 
UUID of the journal entry used to record how the transaction was processed.

There are 3 types of rolons: application rolons, journal entries
and indexes. Application rolons have a type-4 UUID (random), while 
journal entries have a type-1 UUID (timestamp) and indexes have a
type-4 UUID (text).

Think of a rolon as having an immutable structure where all past values are accessible
via the UUID's of the transactions that created them. And these are the same 
UUID's used to identify the journal entries which record how a transaction was processed.

## A Rolon Value

A rolon value preserves both the values of the rolon's properties at a given time, as well as
the UUID of the transaction which assigned the value to each property.

Rolon values may be persisted, but they may also be recreated given the transaction which
created them and the previous state of the ark. Accessing an ark for a given time then may involve
bring an earlier state forward by reprocessing selected transactions.

## A Journal Entry

A journal entry is a rolon which reflects the processing of a transaction.
Only journal entries have type 1 UUIDs, allowing all journal entries to be
accessed in order (or reverse order) via the sorted map of the ark.

A non-performing journal entry reflects only its own creation,
while performing journal entries have a classifier which holds a list of UUID's
of all the other rolons created or updated by this journal entry.

## An Index

An index is a rolon. Its UUID is created from the name of a classifier.
An index has a descriptor which holds a map, keyed by classifier value,
of all rolons which are assigned that value.

## Time Navigation

All queries are based on the current time, which by default is the time of the last
transaction. But any time can be selected. But after a time has been selected, an ark
can no longer be used as the result of a transaction.
