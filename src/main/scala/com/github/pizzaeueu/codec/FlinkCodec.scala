package com.github.pizzaeueu.codec

import com.github.pizzaeueu.domain._
import zio.schema.{DeriveSchema, Schema}

trait FlinkCodec {
  implicit val sessionIdSchema: Schema[Session] = DeriveSchema.gen[Session]
  implicit val statementSchema: Schema[Statement] = DeriveSchema.gen[Statement]
  implicit val statementStatusSchema: Schema[StatementStatus] =
    DeriveSchema.gen[StatementStatus]

  implicit val createStatementSchema: Schema[CreateStatement] =
    DeriveSchema.gen[CreateStatement]

}
