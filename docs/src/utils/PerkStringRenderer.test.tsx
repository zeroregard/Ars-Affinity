import { describe, it, expect } from 'vitest'
import { render, screen } from '@testing-library/react'
import { TestWrapper } from '../test/setup'
import { PerkStringRenderer, formatPerkString } from './PerkStringRenderer'

describe("PerkStringRenderer", () => {
  describe("water perks", () => {
    // PASSIVE_DEHYDRATED has been removed from the perk system

    it("should render PASSIVE_WATER_POWER correctly", () => {
      const perk = {
        perk: "PASSIVE_WATER_POWER",
        amount: 1,
        isBuff: true
      }
      render(
        <TestWrapper>
          <PerkStringRenderer perk={perk} />
        </TestWrapper>
      )
      
      expect(screen.getByText(/\+ Adds \+1 to your Water Power/)).toBeInTheDocument()
    })

    it("should render PASSIVE_WATER_RESISTANCE correctly", () => {
      const perk = {
        perk: "PASSIVE_WATER_RESISTANCE",
        amount: 1,
        isBuff: true
      }
      render(
        <TestWrapper>
          <PerkStringRenderer perk={perk} />
        </TestWrapper>
      )
      
      expect(screen.getByText(/\+ Adds \+1 to your Water Resistance/)).toBeInTheDocument()
    })

    it("should render PASSIVE_HYDRATION correctly", () => {
      const perk = {
        perk: "PASSIVE_HYDRATION",
        amount: 1.0,
        isBuff: true,
        time: 400
      }
      render(
        <TestWrapper>
          <PerkStringRenderer perk={perk} />
        </TestWrapper>
      )
      
      expect(screen.getByText(/\+ Restore 1 hunger down to every 20 seconds when wet/)).toBeInTheDocument()
    })

    it("should render PASSIVE_COLD_WALKER correctly", () => {
      const perk = {
        perk: "PASSIVE_COLD_WALKER",
        amount: 0.10,
        isBuff: true
      }
      render(
        <TestWrapper>
          <PerkStringRenderer perk={perk} />
        </TestWrapper>
      )
      
      expect(screen.getByText(/\+ Nullify friction and move 10% faster on cold surfaces\./)).toBeInTheDocument()
    })

    it("should render ACTIVE_ICE_BLAST correctly", () => {
      const perk = {
        perk: "ACTIVE_ICE_BLAST",
        isBuff: true,
        manaCost: 0.2,
        cooldown: 200,
        damage: 8.0,
        freezeTime: 100,
        radius: 6.0
      }
      render(
        <TestWrapper>
          <PerkStringRenderer perk={perk} />
        </TestWrapper>
      )
      
      expect(screen.getByText(/\+ Blast your surroundings with ice\. Press KEYBIND to activate\. 10 second cooldown/)).toBeInTheDocument()
    })
  })

  describe("fire perks", () => {
    // PASSIVE_DOUSED has been removed from the perk system

    it("should render PASSIVE_FIRE_POWER correctly", () => {
      const perk = {
        perk: "PASSIVE_FIRE_POWER",
        amount: 1,
        isBuff: true
      }
      render(
        <TestWrapper>
          <PerkStringRenderer perk={perk} />
        </TestWrapper>
      )
      
      expect(screen.getByText(/\+ Adds \+1 to your Fire Power/)).toBeInTheDocument()
    })

    it("should render PASSIVE_FIRE_THORNS correctly", () => {
      const perk = {
        perk: "PASSIVE_FIRE_THORNS",
        amount: 0.25,
        isBuff: true
      }
      render(
        <TestWrapper>
          <PerkStringRenderer perk={perk} />
        </TestWrapper>
      )
      
      expect(screen.getByText(/\+ 25% chance to ignite enemies when attacked/)).toBeInTheDocument()
    })

    it("should render PASSIVE_FIRE_RESISTANCE correctly", () => {
      const perk = {
        perk: "PASSIVE_FIRE_RESISTANCE",
        amount: 1,
        isBuff: true
      }
      render(
        <TestWrapper>
          <PerkStringRenderer perk={perk} />
        </TestWrapper>
      )
      
      expect(screen.getByText(/\+ Adds \+1 to your Fire Resistance/)).toBeInTheDocument()
    })

    it("should render ACTIVE_FIRE_DASH correctly", () => {
      const perk = {
        perk: "ACTIVE_FIRE_DASH",
        manaCost: 0.2,
        cooldown: 100,
        dashLength: 10.0,
        dashDuration: 0.3,
        isBuff: true
      }
      render(
        <TestWrapper>
          <PerkStringRenderer perk={perk} />
        </TestWrapper>
      )
      
      expect(screen.getByText(/\+ Dash forward leaving a trail of flames, shooting blaze fireballs at entities in your path\. Press KEYBIND to activate\. 5 second cooldown/)).toBeInTheDocument()
    })
  })

  describe("earth perks", () => {
    // PASSIVE_GROUNDED has been removed from the perk system

    it("should render PASSIVE_EARTH_POWER correctly", () => {
      const perk = {
        perk: "PASSIVE_EARTH_POWER",
        amount: 1,
        isBuff: true
      }
      render(
        <TestWrapper>
          <PerkStringRenderer perk={perk} />
        </TestWrapper>
      )
      
      expect(screen.getByText(/\+ Adds \+1 to your Earth Power/)).toBeInTheDocument()
    })

    it("should render PASSIVE_EARTH_RESISTANCE correctly", () => {
      const perk = {
        perk: "PASSIVE_EARTH_RESISTANCE",
        amount: 1,
        isBuff: true
      }
      render(
        <TestWrapper>
          <PerkStringRenderer perk={perk} />
        </TestWrapper>
      )
      
      expect(screen.getByText(/\+ Adds \+1 to your Earth Resistance/)).toBeInTheDocument()
    })

    it("should render PASSIVE_STONE_SKIN correctly", () => {
      const perk = {
        perk: "PASSIVE_STONE_SKIN",
        time: 400,
        isBuff: true
      }
      render(
        <TestWrapper>
          <PerkStringRenderer perk={perk} />
        </TestWrapper>
      )
      
      expect(screen.getByText(/Melee attacks against you are negated \(20 second cooldown\)/)).toBeInTheDocument()
    })

    it("should render ACTIVE_GROUND_SLAM correctly", () => {
      const perk = {
        perk: "ACTIVE_GROUND_SLAM",
        manaCost: 0.2,
        cooldown: 200,
        isBuff: true
      }
      render(
        <TestWrapper>
          <PerkStringRenderer perk={perk} />
        </TestWrapper>
      )
      
      expect(screen.getByText(/\+ Slam the ground, creating a shockwave that damages and knocks back nearby entities\. Press KEYBIND to activate\. 10 second cooldown/)).toBeInTheDocument()
    })
  })

  describe("air perks", () => {
    // PASSIVE_BURIED has been removed from the perk system

    it("should render PASSIVE_AIR_POWER correctly", () => {
      const perk = {
        perk: "PASSIVE_AIR_POWER",
        amount: 1,
        isBuff: true
      }
      render(
        <TestWrapper>
          <PerkStringRenderer perk={perk} />
        </TestWrapper>
      )
      
      expect(screen.getByText(/\+ Adds \+1 to your Air Power/)).toBeInTheDocument()
    })

    it("should render PASSIVE_AIR_RESISTANCE correctly", () => {
      const perk = {
        perk: "PASSIVE_AIR_RESISTANCE",
        amount: 1,
        isBuff: true
      }
      render(
        <TestWrapper>
          <PerkStringRenderer perk={perk} />
        </TestWrapper>
      )
      
      expect(screen.getByText(/\+ Adds \+1 to your Air Resistance/)).toBeInTheDocument()
    })

    it("should render ACTIVE_AIR_DASH correctly", () => {
      const perk = {
        perk: "ACTIVE_AIR_DASH",
        manaCost: 0.2,
        cooldown: 100,
        isBuff: true
      }
      render(
        <TestWrapper>
          <PerkStringRenderer perk={perk} />
        </TestWrapper>
      )
      
      expect(screen.getByText(/\+ Dash forward in the direction you're looking, shooting Wind Charges at entities in your path\. Press KEYBIND to activate\. 5 second cooldown/)).toBeInTheDocument()
    })
  })

  describe("abjuration perks", () => {
    it("should render PASSIVE_ABJURATION_POWER correctly", () => {
      const perk = {
        perk: "PASSIVE_ABJURATION_POWER",
        amount: 1,
        isBuff: true
      }
      render(
        <TestWrapper>
          <PerkStringRenderer perk={perk} />
        </TestWrapper>
      )
      
      expect(screen.getByText(/\+ Adds \+1 to your Abjuration Power/)).toBeInTheDocument()
    })

    it("should render PASSIVE_ABJURATION_RESISTANCE correctly", () => {
      const perk = {
        perk: "PASSIVE_ABJURATION_RESISTANCE",
        amount: 1,
        isBuff: true
      }
      render(
        <TestWrapper>
          <PerkStringRenderer perk={perk} />
        </TestWrapper>
      )
      
      expect(screen.getByText(/\+ Adds \+1 to your Abjuration Resistance/)).toBeInTheDocument()
    })

    it("should render PASSIVE_HEALING_AMPLIFICATION correctly", () => {
      const perk = {
        perk: "PASSIVE_HEALING_AMPLIFICATION",
        amount: 0.25,
        isBuff: true
      }
      render(
        <TestWrapper>
          <PerkStringRenderer perk={perk} />
        </TestWrapper>
      )
      
      expect(screen.getByText(/\+ Amplifies Heal by 25% and reduces its food cost/)).toBeInTheDocument()
    })

    it("should render PASSIVE_GHOST_STEP correctly", () => {
      const perk = {
        perk: "PASSIVE_GHOST_STEP",
        amount: 0.20,
        time: 10,
        cooldown: 300,
        isBuff: true
      }
      render(
        <TestWrapper>
          <PerkStringRenderer perk={perk} />
        </TestWrapper>
      )
      
      expect(screen.getByText(/\+ On death, cancel death and heal 20% health, become invisible for 0.5 seconds, create decoy \(15 second cooldown\)/)).toBeInTheDocument()
    })

    it("should render ACTIVE_SANCTUARY correctly", () => {
      const perk = {
        perk: "ACTIVE_SANCTUARY",
        manaCost: 0.2,
        cooldown: 100,
        isBuff: true
      }
      render(
        <TestWrapper>
          <PerkStringRenderer perk={perk} />
        </TestWrapper>
      )
      
      expect(screen.getByText(/\+ Hold KEYBIND to project a protective field\. 5 second cooldown/)).toBeInTheDocument()
    })
  })

  describe("conjuration perks", () => {
    it("should render PASSIVE_CONJURATION_RESISTANCE correctly", () => {
      const perk = {
        perk: "PASSIVE_CONJURATION_RESISTANCE",
        amount: 1,
        isBuff: true
      }
      render(
        <TestWrapper>
          <PerkStringRenderer perk={perk} />
        </TestWrapper>
      )
      
      expect(screen.getByText(/\+ Adds \+1 to your Conjuration Resistance/)).toBeInTheDocument()
    })

    it("should render PASSIVE_SUMMON_HEALTH correctly", () => {
      const perk = {
        perk: "PASSIVE_SUMMON_HEALTH",
        amount: 1,
        time: 400,
        isBuff: true
      }
      render(
        <TestWrapper>
          <PerkStringRenderer perk={perk} />
        </TestWrapper>
      )
      
      expect(screen.getByText(/\+ Grants summoned creatures \+1 health boost for 20 seconds/)).toBeInTheDocument()
    })

    it("should render PASSIVE_SUMMON_DEFENSE correctly", () => {
      const perk = {
        perk: "PASSIVE_SUMMON_DEFENSE",
        isBuff: true
      }
      render(
        <TestWrapper>
          <PerkStringRenderer perk={perk} />
        </TestWrapper>
      )
      
      expect(screen.getByText(/Equips summoned creatures with armor/)).toBeInTheDocument()
    })

    it("should render PASSIVE_SUMMONING_POWER correctly", () => {
      const perk = {
        perk: "PASSIVE_SUMMONING_POWER",
        amount: 1,
        isBuff: true
      }
      render(
        <TestWrapper>
          <PerkStringRenderer perk={perk} />
        </TestWrapper>
      )
      
      expect(screen.getByText(/Adds \+1 to your Summoning Power/)).toBeInTheDocument()
    })

    it("should render PASSIVE_MANIPULATION_SICKNESS correctly", () => {
      const perk = {
        perk: "PASSIVE_MANIPULATION_SICKNESS",
        time: 200,
        isBuff: false
      }
      render(
        <TestWrapper>
          <PerkStringRenderer perk={perk} />
        </TestWrapper>
      )
      
      expect(screen.getByText(/- Casting manipulation spells applies sickness for 10 seconds and costs hunger/)).toBeInTheDocument()
    })
  })

  describe("necromancy perks", () => {
    it("should render PASSIVE_ANIMA_POWER correctly", () => {
      const perk = {
        perk: "PASSIVE_ANIMA_POWER",
        amount: 1,
        isBuff: true
      }
      render(
        <TestWrapper>
          <PerkStringRenderer perk={perk} />
        </TestWrapper>
      )
      
      expect(screen.getByText(/\+ Adds \+1 to your Anima Power/)).toBeInTheDocument()
    })

    it("should render PASSIVE_ANIMA_RESISTANCE correctly", () => {
      const perk = {
        perk: "PASSIVE_ANIMA_RESISTANCE",
        amount: 1,
        isBuff: true
      }
      render(
        <TestWrapper>
          <PerkStringRenderer perk={perk} />
        </TestWrapper>
      )
      
      expect(screen.getByText(/\+ Adds \+1 to your Anima Resistance/)).toBeInTheDocument()
    })

    it("should render PASSIVE_ROTTING_GUISE correctly", () => {
      const perk = {
        perk: "PASSIVE_ROTTING_GUISE",
        isBuff: true
      }
      render(
        <TestWrapper>
          <PerkStringRenderer perk={perk} />
        </TestWrapper>
      )
      
      expect(screen.getByText(/\+ Undead creatures ignore you/)).toBeInTheDocument()
    })

    it("should render PASSIVE_BLIGHTED correctly", () => {
      const perk = {
        perk: "PASSIVE_BLIGHTED",
        amount: 0.25,
        isBuff: false
      }
      render(
        <TestWrapper>
          <PerkStringRenderer perk={perk} />
        </TestWrapper>
      )
      
      expect(screen.getByText(/- Healing spells are 25% less effective and add nausea/)).toBeInTheDocument()
    })

    it("should render PASSIVE_LICH_FEAST correctly", () => {
      const perk = {
        perk: "PASSIVE_LICH_FEAST",
        health: 0.5,
        hunger: 0.25,
        isBuff: true
      }
      render(
        <TestWrapper>
          <PerkStringRenderer perk={perk} />
        </TestWrapper>
      )
      
      expect(screen.getByText(/\+ Restore 0\.5 health and 0\.3 hunger when killing non-undead enemies/)).toBeInTheDocument()
    })

    it("should render PASSIVE_SOULSPIKE correctly", () => {
      const perk = {
        perk: "PASSIVE_SOULSPIKE",
        amount: 0.20,
        isBuff: true
      }
      render(
        <TestWrapper>
          <PerkStringRenderer perk={perk} />
        </TestWrapper>
      )
      
      expect(screen.getByText(/\+ Up to 20% chance to Charm\/Weak attackers\./)).toBeInTheDocument()
    })

    it("should render ACTIVE_CURSE_FIELD correctly", () => {
      const perk = {
        perk: "ACTIVE_CURSE_FIELD",
        manaCost: 0.2,
        cooldown: 100,
        isBuff: true
      }
      render(
        <TestWrapper>
          <PerkStringRenderer perk={perk} />
        </TestWrapper>
      )
      
      expect(screen.getByText(/ Hold KEYBIND to project a cursed field that damages and silences foes\. 5 second cooldown/)).toBeInTheDocument()
    })
  })

  describe("manipulation perks", () => {
    it("should render PASSIVE_MANIPULATION_POWER correctly", () => {
      const perk = {
        perk: "PASSIVE_MANIPULATION_POWER",
        amount: 1,
        isBuff: true
      }
      render(
        <TestWrapper>
          <PerkStringRenderer perk={perk} />
        </TestWrapper>
      )
      
      expect(screen.getByText(/\+ Adds \+1 to your Manipulation Power/)).toBeInTheDocument()
    })

    it("should render PASSIVE_MANIPULATION_RESISTANCE correctly", () => {
      const perk = {
        perk: "PASSIVE_MANIPULATION_RESISTANCE",
        amount: 1,
        isBuff: true
      }
      render(
        <TestWrapper>
          <PerkStringRenderer perk={perk} />
        </TestWrapper>
      )
      
      expect(screen.getByText(/\+ Adds \+1 to your Manipulation Resistance/)).toBeInTheDocument()
    })

    it("should render PASSIVE_MANA_TAP correctly", () => {
      const perk = {
        perk: "PASSIVE_MANA_TAP",
        amount: 1.0,
        isBuff: true
      }
      render(
        <TestWrapper>
          <PerkStringRenderer perk={perk} />
        </TestWrapper>
      )
      
      expect(screen.getByText(/\+ Restore 100% of spell damage dealt as mana/)).toBeInTheDocument()
    })

    it("should render PASSIVE_DEFLECTION correctly", () => {
      const perk = {
        perk: "PASSIVE_DEFLECTION",
        time: 120,
        isBuff: true
      }
      render(
        <TestWrapper>
          <PerkStringRenderer perk={perk} />
        </TestWrapper>
      )
      
      expect(screen.getByText(/Incoming projectiles reverse direction and velocity \(6 second cooldown\)/)).toBeInTheDocument()
    })

    it("should render ACTIVE_SWAP_ABILITY correctly", () => {
      const perk = {
        perk: "ACTIVE_SWAP_ABILITY",
        manaCost: 0.2,
        cooldown: 60,
        isBuff: true
      }
      render(
        <TestWrapper>
          <PerkStringRenderer perk={perk} />
        </TestWrapper>
      )
      
      expect(screen.getByText(/\+ Swap positions with entities\. Press KEYBIND to activate\. 3 second cooldown/)).toBeInTheDocument()
    })

    it("should render PASSIVE_UNSTABLE_SUMMONING correctly", () => {
      const perk = {
        perk: "PASSIVE_UNSTABLE_SUMMONING",
        amount: 0.33,
        isBuff: false
      }
      render(
        <TestWrapper>
          <PerkStringRenderer perk={perk} />
        </TestWrapper>
      )
      
      expect(screen.getByText(/- 33% chance to transform summons into random creatures/)).toBeInTheDocument()
    })
  })
})