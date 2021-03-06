package scroll.tests

import mocks.{CoreA, SomeCompartment}
import org.scalatest.{FeatureSpec, GivenWhenThen, Matchers}

class RoleRestrictionsTest extends FeatureSpec with GivenWhenThen with Matchers {
  info("Test spec for role restrictions.")

  feature("Specifying role restrictions") {
    scenario("Validating role restrictions based on role types") {
      Given("A natural, some role instances")
      val player = new CoreA()
      When("A role restriction is specified")
      new SomeCompartment() {
        val roleA = new RoleA()
        val roleD = new RoleD()
        And("some role type specifications are given")
        RoleRestriction[CoreA, RoleA]

        Then("All role restriction should hold")
        player play roleA

        player drop roleA
        When("A role restriction is specified that could not be hold")
        ReplaceRoleRestriction[CoreA, RoleD]
        Then("A runtime exception is expected")
        a[RuntimeException] should be thrownBy {
          player play roleA
        }
      }
    }

    scenario("Validating role restrictions based on multiple role types") {
      Given("A natural, some role instances")
      val player = new CoreA()
      When("Multiple role restrictions are specified")
      new SomeCompartment() {
        val roleA = new RoleA()
        val roleD = new RoleD()
        RoleRestriction[CoreA, RoleA]
        RoleRestriction[CoreA, RoleD]

        Then("All role restriction should hold")
        player play roleA
        player play roleD

        When("A role restriction is specified that could not be hold")
        Then("A runtime exception is expected")
        a[RuntimeException] should be thrownBy {
          player play new RoleB()
        }
      }
    }

    scenario("Validating role restrictions based on custom types") {
      Given("A natural, some role instances")
      val player = new CoreA()
      When("A role restriction is specified")
      new SomeCompartment() {
        val roleA = new RoleA()
        val roleD = new RoleD()
        And("some role type specifications are given")
        type RoleTypeA = {def a(): Int}
        type RoleTypeB = {def a(i: Int, s: String): Int}
        type RoleTypeD = {def update(vA: String, vB: Int): Unit}
        RoleRestriction[CoreA, RoleTypeA]

        Then("All role restriction should hold")
        player play roleA

        When("A restriction is not met")
        player drop roleA
        ReplaceRoleRestriction[CoreA, RoleTypeB]
        Then("A runtime exception is expected")
        a[RuntimeException] should be thrownBy {
          player play roleA
        }

        When("A restriction is not met")
        player drop roleA
        ReplaceRoleRestriction[CoreA, RoleTypeD]
        Then("A runtime exception is expected")
        a[RuntimeException] should be thrownBy {
          player play roleA
        }
        And("The correct role playing should be succeed.")
        player play roleD
      }
    }
  }
}
